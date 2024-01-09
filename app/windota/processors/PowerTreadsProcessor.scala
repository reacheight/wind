package windota.processors

import skadistats.clarity.model.{CombatLogEntry, Entity}
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import windota.Util
import windota.Util._
import windota.extensions._
import windota.models._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class PowerTreadsProcessor extends ProcessorBase {
  private val PT_ON_INT_MANA_GAIN = 120

  private val resourceItems: Set[String] = Set("item_bottle", "item_magic_stick", "item_magic_wand")

  private val _abilityUsageCount: mutable.Map[PlayerId, Int] = mutable.Map.empty
  private val _ptOnIntAbilityUsageCount: mutable.Map[PlayerId, Int] = mutable.Map.empty
  private val _manaLostNoToggling: mutable.Map[PlayerId, Float] = mutable.Map.empty

  def abilityUsageCount: Map[PlayerId, Int] = _abilityUsageCount.toMap
  def ptOnIntAbilityUsageCount: Map[PlayerId, Int] = _ptOnIntAbilityUsageCount.toMap
  def manaLostNoToggling: Map[PlayerId, Float] = _manaLostNoToggling.toMap

  var resourceItemUsages: Map[PlayerId, Int] = Map()
  var ptOnAgilityResourceItemUsages: Map[PlayerId, Int] = Map()

  def ptNotOnStrength: Seq[(GameTimeState, PlayerId)] = _ptNotOnStrength.toSeq
  private val _ptNotOnStrength: ListBuffer[(GameTimeState, PlayerId)] = ListBuffer.empty

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
    if (cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_ABILITY && cle.getAttackerName.startsWith("npc_dota_hero")) {
      val userPlayerId = HeroProcessor.combatLogNameToPlayerId.getOrElse(cle.getAttackerName, -1)
      if (userPlayerId == -1) return

      val heroEntity = Entities.getByPredicate(e => e.isHero && e.playerId.id == userPlayerId)
      val ptOpt = ItemsHelper.findItem(heroEntity, "CDOTA_Item_PowerTreads")
      ptOpt.foreach(pt => {
        val ptStat = pt.getProperty[Int]("m_iStat")
        incrementAbilityUsage(cle, ptStat, heroEntity)
      })
    }

//      if (cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_ITEM && resourceItems.contains(cle.getInflictorName))
//        incrementResourceItemUsage(ptStat, userPlayerId)
  }

  private def incrementAbilityUsage(cle: CombatLogEntry, ptStat: Int, hero: Entity): Unit = {
    val enemiesAround = Entities.filter(e => e.isHero && e.team != hero.team && e.isAlive && Util.getDistance(e, hero) <= 3000)
    if (enemiesAround.nonEmpty)
      return

    val abilityName = cle.getInflictorName
    val abilityOpt = AbilitiesHelper.findAbility(hero, abilityName, shouldBeLearned = true)
    abilityOpt.filter(_.manaCost > 50).foreach(ability => {
      if (ptStat == 1) {
        _ptOnIntAbilityUsageCount(hero.playerId) = _ptOnIntAbilityUsageCount.getOrElse(hero.playerId, 0) + 1
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

        _manaLostNoToggling(hero.playerId) = _manaLostNoToggling.getOrElse(hero.playerId, 0f) + manaLost
      }

      _abilityUsageCount(hero.playerId) = _abilityUsageCount.getOrElse(hero.playerId, 0) + 1
    })
  }

  private def incrementResourceItemUsage(ptStat: Int, userPlayerId: PlayerId): Unit = {
    if (ptStat == 2)
      ptOnAgilityResourceItemUsages += (userPlayerId -> (ptOnAgilityResourceItemUsages(userPlayerId) + 1))

    resourceItemUsages += (userPlayerId -> (resourceItemUsages(userPlayerId) + 1))
  }
}
