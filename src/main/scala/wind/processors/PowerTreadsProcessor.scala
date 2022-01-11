package wind
package processors

import skadistats.clarity.model.{CombatLogEntry, Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityCreated, OnEntityPropertyChanged}
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import wind.models.PlayerId

import scala.collection.mutable.ListBuffer

class PowerTreadsProcessor {
  private var combatLogHeroNameToPlayerId = Map[String, Int]()
  private var powerTreadHandles = Map[Int, Int]()
  private val resourceItems: Set[String] = Set("item_bottle", "item_magic_stick", "item_magic_wand")


  var abilityUsageCount: Map[Int, Int] = Map()
  var ptOnIntAbilityUsageCount: Map[Int, Int] = Map()

  var resourceItemUsages: Map[Int, Int] = Map()
  var ptOnAgilityResourceItemUsages: Map[Int, Int] = Map()

  def ptNotOnStrength: Seq[(GameTimeState, PlayerId)] = _ptNotOnStrength.toSeq
  private val _ptNotOnStrength: ListBuffer[(GameTimeState, PlayerId)] = ListBuffer.empty

  @OnEntityCreated(classPattern = "CWorld")
  def init(ctx: Context, e: Entity): Unit = {
    combatLogHeroNameToPlayerId = ctx.getProcessor(classOf[HeroProcessor]).combatLogNameToPlayerId
  }

  @OnEntityCreated(classPattern = "CDOTA_Item_PowerTreads")
  def onPowerTreadsCreated(ctx: Context, powerTreads: Entity): Unit = {
    val ownerHandle = powerTreads.getProperty[Int]("m_hOwnerEntity")
    val owner = ctx.getProcessor(classOf[Entities]).getByHandle(ownerHandle)
    if (owner == null || !Util.isHero(owner)) return

    val playerId = powerTreads.getProperty[Int]("m_iPlayerOwnerID")
    powerTreadHandles += (playerId -> powerTreads.getHandle)
    abilityUsageCount += (playerId -> 0)
    ptOnIntAbilityUsageCount += (playerId -> 0)
    resourceItemUsages += (playerId -> 0)
    ptOnAgilityResourceItemUsages += (playerId -> 0)
  }

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(ctx: Context, hero: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    if (!Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 1) return

    val playerId = hero.getProperty[Int]("m_iPlayerID")
    if (!powerTreadHandles.contains(playerId)) return

    val entities = ctx.getProcessor(classOf[Entities])
    val gameRules = entities.getByDtName("CDOTAGamerulesProxy")
    if (Util.getSpawnTime(hero, gameRules.getProperty[Float]("m_pGameRules.m_fGameTime")) < 10) return

    val powerTreadsEntity = entities.getByHandle(powerTreadHandles(playerId))
    if (powerTreadsEntity == null) {
      powerTreadHandles -= playerId
      return
    }

    val time = Util.getGameTimeState(gameRules)
    val ptStat = powerTreadsEntity.getProperty[Int]("m_iStat")
    if (ptStat != 0)
      _ptNotOnStrength.addOne((time, PlayerId(playerId)))
  }

  @OnCombatLogEntry
  def onCombatLogEntry(ctx: Context, cle: CombatLogEntry): Unit = {
    val userPlayerId = combatLogHeroNameToPlayerId.getOrElse(cle.getAttackerName, -1)
    if (!powerTreadHandles.contains(userPlayerId)) return

    val powerTreadsEntity = ctx.getProcessor(classOf[Entities]).getByHandle(powerTreadHandles(userPlayerId))
    if (powerTreadsEntity == null) {
      powerTreadHandles -= userPlayerId
      return
    }

    val ptStat = powerTreadsEntity.getProperty[Int]("m_iStat")

    if (cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_ABILITY)
      incrementAbilityUsage(ptStat, userPlayerId)

    if (cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_ITEM && resourceItems.contains(cle.getInflictorName))
      incrementResourceItemUsage(ptStat, userPlayerId)
  }

  def incrementAbilityUsage(ptStat: Int, userPlayerId: Int): Unit = {
    if (ptStat == 1) {
      ptOnIntAbilityUsageCount += (userPlayerId ->  (ptOnIntAbilityUsageCount(userPlayerId) + 1))
    }

    abilityUsageCount += (userPlayerId ->  (abilityUsageCount(userPlayerId) + 1))
  }

  def incrementResourceItemUsage(ptStat: Int, userPlayerId: Int): Unit = {
    if (ptStat == 2)
      ptOnAgilityResourceItemUsages += (userPlayerId -> (ptOnAgilityResourceItemUsages(userPlayerId) + 1))

    resourceItemUsages += (userPlayerId -> (resourceItemUsages(userPlayerId) + 1))
  }
}
