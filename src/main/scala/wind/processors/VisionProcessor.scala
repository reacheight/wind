package wind.processors

import skadistats.clarity.model.{CombatLogEntry, Entity}
import skadistats.clarity.processor.entities.{Entities, OnEntityCreated, OnEntityPropertyChanged}
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import wind.models.{GameTimeState, Location, Observer, PlayerId}
import wind.Util
import wind.extensions._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class VisionProcessor {
  val OBS_TTL = 360
  val EPS = 0.01

  def smokeUsedOnVision: Seq[(GameTimeState, PlayerId)] = itemUsages("item_smoke_of_deceit").toSeq
  def observerPlacedOnVision: Seq[Observer] =
    itemUsages("item_ward_observer")
      .toSeq
      .flatMap { case (time, owner) => observers.find(obs => obs.created == time && obs.owner == owner) }

  def observers: Seq[Observer] = _observers.toMap.values.toSeq

  private val itemUsages = mutable.Map(
    "item_smoke_of_deceit" -> ListBuffer.empty[(GameTimeState, PlayerId)],
    "item_ward_observer" -> ListBuffer.empty[(GameTimeState, PlayerId)]
  )

  private val _observers = mutable.Map.empty[Int, Observer]

  @OnEntityCreated(classPattern = "CDOTA_NPC_Observer_Ward")
  private def onObserverPlaced(ctx: Context, observer: Entity): Unit = {
    val time = Util.getGameTimeState(ctx.getProcessor(classOf[Entities]).getByDtName("CDOTAGamerulesProxy"))
    val location = Util.getLocation(observer)
    val owner = PlayerId(observer.get[Int]("m_nPlayerOwnerID").get)
    _observers(observer.getHandle) = Observer(observer.getHandle, location, time, time, owner)
  }

  @OnEntityPropertyChanged(classPattern = "CDOTA_NPC_Observer_Ward", propertyPattern = "m_lifeState")
  private def onObserverEnded(ctx: Context, observer: Entity, fp: FieldPath): Unit = {
    val time = Util.getGameTimeState(ctx.getProcessor(classOf[Entities]).getByDtName("CDOTAGamerulesProxy"))
    val current = _observers(observer.getHandle)
    _observers(observer.getHandle) = current.copy(ended = time)
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
