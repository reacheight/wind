package windota.processors

import skadistats.clarity.model.{CombatLogEntry, Entity}
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.processor.stringtables.{StringTables, UsesStringTable}
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import windota.Util
import windota.Util.EntityExtension2
import windota.constants.{Abilities, Clarity, Items}
import windota.extensions.{EntityExtension, FieldPath}
import windota.models.{DamageType, PlayerId}
import windota.models.internal.{DamageAmount, DamageEventData, DeathSummaryDamageDealt, DeathSummaryData}

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
      val abilityOpt = Abilities.findId(cle.getInflictorName)
      val itemOpt = Items.findId(cle.getInflictorName)

      _damage += DamageEventData(target, attacker, damageAmount, GameTimeHelper.State, abilityOpt, itemOpt, DamageType.fromCLValue(cle.getDamageType))
    }
  }

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(hero: Entity, fp: FieldPath): Unit = {
    if (!hero.isHero || hero.get[Int](fp) != 1 || hero.getSpawnTime(GameTimeHelper.State) < 6) return

    for {
      playerIdRaw <- hero.get[Int](Clarity.Property.Names.PlayerId)
    } yield {
      val playerId = PlayerId(playerIdRaw)
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

      _deaths += DeathSummaryData(playerId, GameTimeHelper.State, damageReceived, hero.getSpawnTime(GameTimeHelper.State))
      _damage.filterInPlace(d => d.target != playerId)
    }
  }

  private def heroDamagedAnotherHero(cle: CombatLogEntry): Boolean =
    cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_DAMAGE &&
      cle.getDamageSourceName.startsWith("npc_dota_hero") && cle.getTargetName.startsWith("npc_dota_hero") &&
      cle.getDamageSourceName != cle.getTargetName

  private def calculateDamage(damageEvents: ListBuffer[DamageEventData]): DamageAmount = DamageAmount(
    damageEvents.filter(d => d.damageType == DamageType.Physical).map(d => d.amount).sum,
    damageEvents.filter(d => d.damageType == DamageType.Magical).map(d => d.amount).sum,
    damageEvents.filter(d => d.damageType == DamageType.Pure).map(d => d.amount).sum
  )
}
