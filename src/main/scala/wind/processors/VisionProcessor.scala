package wind.processors

import skadistats.clarity.model.{CombatLogEntry, Entity}
import skadistats.clarity.processor.entities.{Entities, OnEntityCreated, OnEntityPropertyChanged}
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import wind.models.{GameTimeState, Location, PlayerId}
import wind.Util
import wind.extensions.FieldPath

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class VisionProcessor {
  def smokeUsedOnVision: Seq[(GameTimeState, PlayerId)] = itemUsages("item_smoke_of_deceit").toSeq
  def observerPlacedOnVision: Seq[(GameTimeState, PlayerId)] = itemUsages("item_ward_observer").toSeq

  def observers: Seq[(Location, GameTimeState, GameTimeState)] = _observers.toMap.values.toSeq

  private val itemUsages = mutable.Map(
    "item_smoke_of_deceit" -> ListBuffer.empty[(GameTimeState, PlayerId)],
    "item_ward_observer" -> ListBuffer.empty[(GameTimeState, PlayerId)]
  )

  private val _observers = mutable.Map.empty[Int, (Location, GameTimeState, GameTimeState)]

  @OnEntityCreated(classPattern = "CDOTA_NPC_Observer_Ward")
  private def onObserverPlaced(ctx: Context, observer: Entity): Unit = {
    val time = Util.getGameTimeState(ctx.getProcessor(classOf[Entities]).getByDtName("CDOTAGamerulesProxy"))
    val location = Util.getLocation(observer)
    _observers(observer.getHandle) = (location, time, time)
  }

  @OnEntityPropertyChanged(classPattern = "CDOTA_NPC_Observer_Ward", propertyPattern = "m_lifeState")
  private def onObserverEnded(ctx: Context, observer: Entity, fp: FieldPath): Unit = {
    val time = Util.getGameTimeState(ctx.getProcessor(classOf[Entities]).getByDtName("CDOTAGamerulesProxy"))
    val current = _observers(observer.getHandle)
    _observers(observer.getHandle) = current.copy(_3 = time)
  }

  @OnCombatLogEntry
  private def onCombatLogEntry(ctx: Context, cle: CombatLogEntry): Unit = {
    if (isItemUsedOnEnemyVision(cle) && itemUsages.contains(cle.getInflictorName)) {
      val heroProcessor = ctx.getProcessor(classOf[HeroProcessor])
      val entities = ctx.getProcessor(classOf[Entities])

      val playerId = heroProcessor.combatLogNameToPlayerId.get(cle.getAttackerName)
      playerId.foreach(id => {
        val gameRules = entities.getByDtName("CDOTAGamerulesProxy")
        itemUsages(cle.getInflictorName).addOne(Util.getGameTimeState(gameRules), PlayerId(id))
      })
    }
  }

  private def isItemUsedOnEnemyVision(cle: CombatLogEntry) =
    cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_ITEM && cle.isVisibleDire && cle.isVisibleRadiant
}
