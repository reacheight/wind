package windota.processors

import skadistats.clarity.model.{CombatLogEntry, Entity}
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import windota.Util
import windota.extensions._
import windota.models._

import scala.collection.mutable

class MidasEfficiencyProcessor extends ProcessorBase {
  def midasEfficiency: Map[PlayerId, Float] = _midasEfficiency

  private val MidasName = "item_hand_of_midas"
  private val MidasCooldown = 90

  private val _midasPurchases: mutable.Map[PlayerId, GameTimeState] = mutable.Map.empty
  private val _midasUsageCount: mutable.Map[PlayerId, Int] = mutable.Map.empty
  private var _midasEfficiency: Map[PlayerId, Float] = Map.empty

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_nGameState")
  def onGameEnded(gameRules: Entity, fp: FieldPath): Unit = {
    val gameState = gameRules.getPropertyForFieldPath[Int](fp)
    if (gameState != 6) return

    _midasEfficiency = _midasPurchases.map { case (playerId, purchaseTime) =>
      val maxPossibleUsageCount = (GameTimeHelper.State.gameTime - purchaseTime.gameTime).toInt / MidasCooldown
      val playerUsageCount = _midasUsageCount(playerId)
      playerId -> (playerUsageCount.toFloat / maxPossibleUsageCount) * 100
    }.toMap
  }

  @OnCombatLogEntry
  def onMidasPurchase(ctx: Context, cle: CombatLogEntry): Unit = {
    if (isMidasPurchase(cle)) {
      val playerId = PlayerId(ctx.getProcessor(classOf[HeroProcessor]).combatLogNameToPlayerId(cle.getTargetName))

      _midasPurchases(playerId) = GameTimeHelper.State
      _midasUsageCount(playerId) = 0
    }
  }

  @OnCombatLogEntry
  def onMidasUsage(ctx: Context, cle: CombatLogEntry): Unit = {
    if (isMidasUsage(cle)) {
      val playerId: PlayerId = PlayerId(ctx.getProcessor(classOf[HeroProcessor]).combatLogNameToPlayerId(cle.getAttackerName))
      _midasUsageCount(playerId) += 1
    }
  }

  def isMidasPurchase(cle: CombatLogEntry): Boolean =
    cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_PURCHASE && cle.getValueName == MidasName

  def isMidasUsage(cle: CombatLogEntry): Boolean =
    cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_ITEM && cle.getInflictorName == MidasName
}
