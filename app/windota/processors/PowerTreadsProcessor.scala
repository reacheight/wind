package windota.processors

import skadistats.clarity.event.Insert
import skadistats.clarity.model.{CombatLogEntry, Entity}
import skadistats.clarity.processor.entities.{OnEntityCreated, OnEntityPropertyChanged}
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import windota.Util
import windota.Util._
import windota.extensions._
import windota.models._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class PowerTreadsProcessor extends EntitiesProcessor {
  private val PT_ON_INT_MANA_GAIN = 120

  private var combatLogHeroNameToPlayerId = Map[String, Int]()
  private var powerTreadHandles = Map[Int, Int]()
  private val resourceItems: Set[String] = Set("item_bottle", "item_magic_stick", "item_magic_wand")

  var _abilityUsageCount: Map[Int, Int] = Map()
  var _ptOnIntAbilityUsageCount: Map[Int, Int] = Map()
  val _manaLostNoToggling: mutable.Map[Int, Float] = mutable.Map()

  def abilityUsageCount: Map[Int, Int] = _abilityUsageCount
  def ptOnIntAbilityUsageCount: Map[Int, Int] = _ptOnIntAbilityUsageCount
  def manaLostNoToggling: Map[Int, Float] = _manaLostNoToggling.toMap

  var resourceItemUsages: Map[Int, Int] = Map()
  var ptOnAgilityResourceItemUsages: Map[Int, Int] = Map()

  def ptNotOnStrength: Seq[(GameTimeState, PlayerId)] = _ptNotOnStrength.toSeq
  private val _ptNotOnStrength: ListBuffer[(GameTimeState, PlayerId)] = ListBuffer.empty

  @Insert
  private val ctx: Context = null

  @OnEntityCreated(classPattern = "CWorld")
  def init(ctx: Context, e: Entity): Unit = {
    combatLogHeroNameToPlayerId = ctx.getProcessor(classOf[HeroProcessor]).combatLogNameToPlayerId
  }

  @OnEntityCreated(classPattern = "CDOTA_Item_PowerTreads")
  def onPowerTreadsCreated(powerTreads: Entity): Unit = {
    val ownerHandle = powerTreads.getProperty[Int]("m_hOwnerEntity")
    val owner = Entities.get(ownerHandle)
    if (owner.exists(Util.isHero)) {
      val playerId = powerTreads.getProperty[Int]("m_iPlayerOwnerID")
      powerTreadHandles += (playerId -> powerTreads.getHandle)
      _abilityUsageCount += (playerId -> 0)
      _ptOnIntAbilityUsageCount += (playerId -> 0)
      _manaLostNoToggling(playerId) = 0
      resourceItemUsages += (playerId -> 0)
      ptOnAgilityResourceItemUsages += (playerId -> 0)
    }
  }

//  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
//  def onHeroDied(hero: Entity, fp: FieldPath): Unit = {
//    if (!Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 1) return
//
//    val playerId = hero.getProperty[Int]("m_iPlayerID")
//    if (!powerTreadHandles.contains(playerId)) return
//
//    val gameRules = Entities.getByDtName("CDOTAGamerulesProxy")
//    if (Util.getSpawnTime(hero, GameTime) < 10) return
//
//    val powerTreads = Entities.get(powerTreadHandles(playerId))
//    powerTreads match {
//      case None => powerTreadHandles -= playerId
//      case Some(entity) =>
//        val time = Util.getGameTimeState(gameRules)
//        val ptStat = entity.getProperty[Int]("m_iStat")
//        if (ptStat != 0)
//          _ptNotOnStrength.addOne((time, PlayerId(playerId)))
//    }
//  }

  @OnCombatLogEntry
  def onCombatLogEntry(cle: CombatLogEntry): Unit = {
    val userPlayerId = combatLogHeroNameToPlayerId.getOrElse(cle.getAttackerName, -1)
    if (!powerTreadHandles.contains(userPlayerId)) return

    val powerTreads = Entities.get(powerTreadHandles(userPlayerId))
    powerTreads match {
      case None => powerTreadHandles -= userPlayerId
      case Some(entity) =>
        val ptStat = entity.getProperty[Int]("m_iStat")

        if (cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_ABILITY)
          incrementAbilityUsage(cle, ptStat, userPlayerId)

        if (cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_ITEM && resourceItems.contains(cle.getInflictorName))
          incrementResourceItemUsage(ptStat, userPlayerId)
    }
  }

  def incrementAbilityUsage(cle: CombatLogEntry, ptStat: Int, userPlayerId: Int): Unit = {
    val abilityUsageProcessor = ctx.getProcessor(classOf[AbilityUsageProcessor])

    val hero = Entities.getByPredicate(e => e.isHero && e.playerId.id == userPlayerId)
    val enemiesAround = Entities.filter(e => e.isHero && e.team != hero.team && e.isAlive && Util.getDistance(e, hero) <= 3000)
    if (enemiesAround.nonEmpty)
      return

    val abilityName = cle.getInflictorName
    val abilities = abilityUsageProcessor.getAbilities(hero)
    val abilityOpt = abilityUsageProcessor.findAbility(abilities, abilityName)

    abilityOpt.filter(_.manaCost > 50).foreach(ability => {
      if (ptStat == 1) {
        _ptOnIntAbilityUsageCount += (userPlayerId -> (_ptOnIntAbilityUsageCount(userPlayerId) + 1))
      } else {
        val manaCost = ability.manaCost
        val heroMaxMana = hero.maxMana
        val heroManaAfterCast = hero.currentMana
        val heroManaBeforeCast = heroManaAfterCast + manaCost
        val heroManaBeforeCastFraction = heroManaBeforeCast / heroMaxMana

        val heroMaxManaWithPtOnInt = heroMaxMana + PT_ON_INT_MANA_GAIN
        val heroManaBeforeCastWithPtOnInt = heroMaxManaWithPtOnInt * heroManaBeforeCastFraction
        val heroManaAfterCastWithPtOnInt = heroManaBeforeCastWithPtOnInt - manaCost
        val heroManaAfterCastWithPtOnIntFraction = heroManaAfterCastWithPtOnInt / heroMaxManaWithPtOnInt

        val heroManaAfterCastWithPtToggle = heroMaxMana * heroManaAfterCastWithPtOnIntFraction
        val manaLost = heroManaAfterCastWithPtToggle - heroManaAfterCast

        _manaLostNoToggling(userPlayerId) = _manaLostNoToggling(userPlayerId) + manaLost
      }

      _abilityUsageCount += (userPlayerId -> (_abilityUsageCount(userPlayerId) + 1))
    })
  }

  def incrementResourceItemUsage(ptStat: Int, userPlayerId: Int): Unit = {
    if (ptStat == 2)
      ptOnAgilityResourceItemUsages += (userPlayerId -> (ptOnAgilityResourceItemUsages(userPlayerId) + 1))

    resourceItemUsages += (userPlayerId -> (resourceItemUsages(userPlayerId) + 1))
  }
}
