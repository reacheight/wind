package wind
package processors

import skadistats.clarity.model.{CombatLogEntry, Entity}
import skadistats.clarity.processor.entities.{Entities, OnEntityCreated}
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES

class PowerTreadsProcessor {
  private var combatLogHeroNameToPlayerId = Map[String, Int]()
  private var powerTreadHandles = Map[Int, Int]()

  var powerTreadsAbilityUsageCount: Map[Int, Int] = Map()
  var powerTreadsOnIntAbilityUsageCount: Map[Int, Int] = Map()

  @OnEntityCreated(classPattern = "CWorld")
  def init(ctx: Context, e: Entity): Unit = {
    combatLogHeroNameToPlayerId = ctx.getProcessor(classOf[HeroProcessor]).combatLogHeroNameToPlayerId
  }

  @OnEntityCreated(classPattern = "CDOTA_Item_PowerTreads")
  def onPowerTreadsCreated(ctx: Context, powerTreads: Entity): Unit = {
    val ownerHandle = powerTreads.getProperty[Int]("m_hOwnerEntity")
    val owner = ctx.getProcessor(classOf[Entities]).getByHandle(ownerHandle)
    if (owner == null || !Util.isHero(owner)) return

    val playerId = powerTreads.getProperty[Int]("m_iPlayerOwnerID")
    powerTreadHandles += (playerId -> powerTreads.getHandle)
    powerTreadsAbilityUsageCount += (playerId -> 0)
    powerTreadsOnIntAbilityUsageCount += (playerId -> 0)
  }

  @OnCombatLogEntry
  def onCombatLogEntry(ctx: Context, cle: CombatLogEntry): Unit = {
    if (cle.getType != DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_ABILITY) return

    val userPlayerId = combatLogHeroNameToPlayerId.getOrElse(cle.getAttackerName, -1)
    if (!powerTreadHandles.contains(userPlayerId)) return

    val powerTreadsEntity = ctx.getProcessor(classOf[Entities]).getByHandle(powerTreadHandles(userPlayerId))
    if (powerTreadsEntity == null) {
      powerTreadHandles -= userPlayerId
      return
    }

    val ptStat = powerTreadsEntity.getProperty[Int]("m_iStat")
    if (ptStat == 1) {
      powerTreadsOnIntAbilityUsageCount += (userPlayerId ->  (powerTreadsOnIntAbilityUsageCount(userPlayerId) + 1))
    }

    powerTreadsAbilityUsageCount += (userPlayerId ->  (powerTreadsAbilityUsageCount(userPlayerId) + 1))
  }
}