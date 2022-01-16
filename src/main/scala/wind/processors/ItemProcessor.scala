package wind.processors

import skadistats.clarity.model.CombatLogEntry
import skadistats.clarity.processor.entities.Entities
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import wind.Util

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class ItemProcessor {
  def purchases: Map[String, Seq[(String, Int)]] = _purchases.map { case (hero, list) => hero -> list.toSeq }.toMap

  private val _purchases: mutable.Map[String, ListBuffer[(String, Int)]] = mutable.Map.empty

  @OnCombatLogEntry
  def onPurchase(ctx: Context, cle: CombatLogEntry): Unit = {
    if (cle.getType != DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_PURCHASE) return

    val gameRules = ctx.getProcessor(classOf[Entities]).getByDtName("CDOTAGamerulesProxy")
    val gameTime = Util.getGameTimeState(gameRules)

    if (!_purchases.contains(cle.getTargetName))
      _purchases(cle.getTargetName) = ListBuffer.empty

    _purchases(cle.getTargetName).addOne((cle.getValueName, gameTime.gameTime.toInt))
  }
}
