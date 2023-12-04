package windota.processors

import skadistats.clarity.model.{CombatLogEntry, Entity}
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import windota.Util.EntityExtension2
import windota.constants.Clarity
import windota.extensions.{EntityExtension, FieldPath}
import windota.models.PlayerId
import windota.models.internal.{DamageEventData, DeathSummaryData}

import scala.collection.mutable.ListBuffer

class DeathsSummaryProcessor extends ProcessorBase {
  private val DEATH_DAMAGE_WINDOW = 30

  private val _damage = ListBuffer.empty[DamageEventData]
  private val _deaths = ListBuffer.empty[DeathSummaryData]

  def deaths: Seq[DeathSummaryData] = _deaths.toList

  @OnCombatLogEntry
  def onHeroDamage(ctx: Context, cle: CombatLogEntry): Unit = {
    if (heroDamagedAnotherHero(cle)) {
      val target = HeroProcessor.clNameToPlayerId(cle.getTargetName)
      val attacker = HeroProcessor.clNameToPlayerId(cle.getDamageSourceName)
      val damageAmount = cle.getValue

      _damage += DamageEventData(target, attacker, damageAmount, GameTimeHelper.State)
    }
  }

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(hero: Entity, fp: FieldPath): Unit = {
    if (!hero.isHero || hero.get[Int](fp) != 1) return

    for {
      playerIdRaw <- hero.get[Int](Clarity.Property.Names.PlayerId)
    } yield {
      val playerId = PlayerId(playerIdRaw)
      val damageReceived = _damage
        .filter(d => d.target == playerId && GameTimeHelper.State.gameTime - d.time.gameTime <= DEATH_DAMAGE_WINDOW)
        .groupBy(d => d.attacker)
        .map { case (attackerId, group) => attackerId -> group.map(d => d.amount).sum }

      _deaths += DeathSummaryData(playerId, GameTimeHelper.State, damageReceived)
      _damage.filterInPlace(d => d.target != playerId)
    }
  }

  private def heroDamagedAnotherHero(cle: CombatLogEntry): Boolean =
    cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_DAMAGE &&
      cle.getDamageSourceName.startsWith("npc_dota_hero") && cle.getTargetName.startsWith("npc_dota_hero") &&
      cle.getDamageSourceName != cle.getTargetName
}
