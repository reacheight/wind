package wind
package processors

import skadistats.clarity.model.{CombatLogEntry, Entity}
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import wind.extensions.FieldPath

import scala.collection.mutable

class SummonsProcessor {
  var summonFeedGold: Map[Int, Int] = Map()
  private val gold = mutable.Map.empty[Float, (Int, Int)]
  private val deaths = mutable.Map.empty[Float, (String, Int)]

  @OnCombatLogEntry
  def onCombatLog(ctx: Context, cle: CombatLogEntry): Unit = {
    if (isSummonKilled(cle))
      deaths.addOne(cle.getTimestamp, (cle.getTargetSourceName, cle.getAttackerNameIdx))

    if (cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_GOLD)
      gold.addOne(cle.getTimestamp, (cle.getTargetNameIdx, cle.getValue))
  }

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_nGameState")
  def onGameEnded(ctx: Context, gameRules: Entity, fp: FieldPath): Unit = {
    val gameState = gameRules.getPropertyForFieldPath[Int](fp)
    if (gameState != 6) return

    val heroes = ctx.getProcessor(classOf[HeroProcessor])

    deaths.foreach { case (deathTime, (target, attacker)) =>
      gold
        .find { case (goldTime, (receiver, _)) => deathTime == goldTime && attacker == receiver }
        .foreach { case (_, (_, gold)) =>
          val playerId = heroes.combatLogNameToPlayerId(target)
          summonFeedGold += playerId -> (summonFeedGold.getOrElse(playerId, 0) + gold)
        }
    }
  }

  private def isSummonKilled(cle: CombatLogEntry): Boolean =
    cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_DEATH &&
      isHeroName(cle.getTargetSourceName) && isHeroName(cle.getAttackerName) &&
      cle.getTargetSourceNameIdx != cle.getAttackerNameIdx && cle.getTargetSourceNameIdx != cle.getTargetNameIdx &&
      !cle.getTargetName.contains("sentry_wards") && !cle.getTargetName.contains("observer_wards") &&
      !cle.getTargetName.contains("courier")

  private def isHeroName(name: String): Boolean = name.startsWith("npc_dota_hero")
}
