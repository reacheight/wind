package wind.processors

import skadistats.clarity.model.CombatLogEntry
import skadistats.clarity.processor.entities.Entities
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import wind.{GameTimeState, Util}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class VisionProcessor {
  private val itemUsages = mutable.Map(
    "item_smoke_of_deceit" -> ListBuffer.empty[(Int, GameTimeState)],
    "item_ward_observer" -> ListBuffer.empty[(Int, GameTimeState)]
  )

  def smokeUsedOnVision: List[(Int, GameTimeState)] = itemUsages("item_smoke_of_deceit").toList
  def observerPlacedOnVision: List[(Int, GameTimeState)] = itemUsages("item_ward_observer").toList

  @OnCombatLogEntry
  private def onCombatLogEntry(ctx: Context, cle: CombatLogEntry): Unit = {
    if (isItemUsedOnEnemyVision(cle) && itemUsages.contains(cle.getInflictorName)) {
      val heroProcessor = ctx.getProcessor(classOf[HeroProcessor])
      val entities = ctx.getProcessor(classOf[Entities])

      val playerId = heroProcessor.combatLogNameToPlayerId.getOrElse(cle.getAttackerName, -1)
      if (playerId >= 0) {
        val gameRules = entities.getByDtName("CDOTAGamerulesProxy")
        itemUsages(cle.getInflictorName) += playerId -> Util.getGameTimeState(gameRules)
      }
    }
  }

  private def isItemUsedOnEnemyVision(cle: CombatLogEntry) =
    cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_ITEM && cle.isVisibleDire && cle.isVisibleRadiant
}