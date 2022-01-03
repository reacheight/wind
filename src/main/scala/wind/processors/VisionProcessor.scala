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
    "item_smoke_of_deceit" -> mutable.Map.empty[Int, ListBuffer[GameTimeState]],
    "item_ward_observer" -> mutable.Map.empty[Int, ListBuffer[GameTimeState]]
  )

  def smokeUsedOnVision: Map[Int, List[GameTimeState]] = itemUsages("item_smoke_of_deceit").map { case(id, times) => id -> times.toList }.toMap
  def observerPlacedOnVision: Map[Int, List[GameTimeState]] = itemUsages("item_ward_observer").map { case(id, times) => id -> times.toList }.toMap

  @OnCombatLogEntry
  private def onCombatLogEntry(ctx: Context, cle: CombatLogEntry): Unit = {
    if (isItemUsedOnEnemyVision(cle) && itemUsages.contains(cle.getInflictorName)) {
      val heroProcessor = ctx.getProcessor(classOf[HeroProcessor])
      val entities = ctx.getProcessor(classOf[Entities])

      val playerId = heroProcessor.combatLogNameToPlayerId.getOrElse(cle.getAttackerName, -1)
      if (playerId >= 0) {
        val gameRules = entities.getByDtName("CDOTAGamerulesProxy")
        itemUsages(cle.getInflictorName).getOrElseUpdate(playerId, ListBuffer.empty).addOne(Util.getGameTimeState(gameRules))
      }
    }
  }

  private def isItemUsedOnEnemyVision(cle: CombatLogEntry) =
    cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_ITEM && cle.isVisibleDire && cle.isVisibleRadiant
}
