package windota.processors

import skadistats.clarity.model.CombatLogEntry
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import windota.Util.EntityExtension2
import windota.constants.{Abilities, Items}
import windota.extensions.EntitiesExtension
import windota.models.DamageType
import windota.models.internal._

import scala.collection.mutable.ListBuffer

class DeathsSummaryProcessor extends ProcessorBase {
  private val DEATH_DAMAGE_WINDOW = 30

  private val _damage = ListBuffer.empty[DamageEventData]
  private val _deaths = ListBuffer.empty[DeathSummaryData]
  private val _gold = ListBuffer.empty[GoldUpdate]

  def deaths: Seq[DeathSummaryData] = _deaths.toList

  @OnCombatLogEntry
  def onGoldReceived(ctx: Context, cle: CombatLogEntry): Unit = {
    if (cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_GOLD && (cle.getGoldReason == 1 || cle.getGoldReason == 12)) {
      val receiver = HeroProcessor.clNameToPlayerId(cle.getTargetName)
      _gold += GoldUpdate(receiver, cle.getValue, cle.getGoldReason, cle.getTimestamp)
    }
  }

  @OnCombatLogEntry
  def onHeroDamage(ctx: Context, cle: CombatLogEntry): Unit = {
    if (heroDamagedAnotherHero(cle)) {
      val target = HeroProcessor.clNameToPlayerId(cle.getTargetName)
      val attacker = HeroProcessor.clNameToPlayerId(cle.getDamageSourceName)
      val damageAmount = cle.getValue
      val abilityOpt = Abilities.findId(cle.getInflictorName)
      val itemOpt = Items.findId(cle.getInflictorName)

      _damage += DamageEventData(target, attacker, damageAmount, GameTimeHelper.State, abilityOpt, itemOpt, DamageType.fromCLValue(cle.getDamageType))
    }
  }

  @OnCombatLogEntry
  def onHeroDied(ctx: Context, cle: CombatLogEntry): Unit = {
    if (cle.getType != DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_DEATH || !cle.getTargetName.startsWith("npc_dota_hero")) return
    val playerId = HeroProcessor.clNameToPlayerId(cle.getTargetName)
    val heroOpt = Entities.find(e => e.isHero && e.playerId == playerId)

    heroOpt.foreach(hero => {
      val damageReceived = _damage
        .filter(d => d.target == playerId && GameTimeHelper.State.gameTime - d.time.gameTime <= DEATH_DAMAGE_WINDOW)
        .groupBy(d => d.attacker)
        .map { case (attackerId, attackerGroup) =>
          val attackDamage = calculateDamage(attackerGroup.filter(d => d.isAttack))
          val abilityDamage = attackerGroup
            .filter(d => d.ability.nonEmpty)
            .groupBy(d => d.ability)
            .map { case (abilityId, abilityGroup) => abilityId.get -> calculateDamage(abilityGroup) }
          val itemDamage = attackerGroup
            .filter(d => d.item.nonEmpty)
            .groupBy(d => d.item)
            .map { case (itemId, itemGroup) => itemId.get -> calculateDamage(itemGroup) }

          attackerId -> DeathSummaryDamageDealt(attackDamage, abilityDamage, itemDamage)
        }

      val goldUpdates = _gold.filter(u => u.clTimestamp == cle.getTimestamp)
      val penalty = goldUpdates.filter(u => u.playerId == playerId && u.reason == 1).map(u => u.amount).sum
      val earnings = goldUpdates.filter(u => u.reason == 12)
        .groupBy(u => u.playerId)
        .map { case (goldReceiver, updates) => goldReceiver -> updates.map(u => u.amount).sum }

      _deaths += DeathSummaryData(playerId, GameTimeHelper.State, cle.getTimestamp, damageReceived, hero.getSpawnTime(GameTimeHelper.State), 0 - penalty, earnings)
      _damage.filterInPlace(d => d.target != playerId)
      _gold.clear()
    })
  }

  private def heroDamagedAnotherHero(cle: CombatLogEntry): Boolean =
    cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_DAMAGE &&
      cle.getDamageSourceName.startsWith("npc_dota_hero") && cle.getTargetName.startsWith("npc_dota_hero") &&
      !cle.isTargetIllusion && !cle.isAttackerIllusion &&
      cle.getDamageSourceName != cle.getTargetName

  private def calculateDamage(damageEvents: ListBuffer[DamageEventData]): DamageAmount = DamageAmount(
    damageEvents.filter(d => d.damageType == DamageType.Physical).map(d => d.amount).sum,
    damageEvents.filter(d => d.damageType == DamageType.Magical).map(d => d.amount).sum,
    damageEvents.filter(d => d.damageType == DamageType.Pure).map(d => d.amount).sum
  )
}
