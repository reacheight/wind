package windota.processors

import skadistats.clarity.model.{CombatLogEntry, Entity}
import skadistats.clarity.processor.entities.{Entities, OnEntityCreated, OnEntityPropertyChanged}
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import windota.Util
import windota.extensions._
import windota.models._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class VisionProcessor extends ProcessorBase {
  val OBS_TTL = 360
  val EPS = 0.01

  def smokeUsedOnVision: Seq[(GameTimeState, PlayerId)] = itemUsages("item_smoke_of_deceit").toSeq
  def observerPlacedOnVision: Seq[Ward] =
    itemUsages("item_ward_observer")
      .toSeq
      .flatMap { case (time, owner) => observers.find(obs => obs.created == time && obs.owner == owner) }

  def observers: Seq[Ward] = _observers.toMap.values.toSeq
  def sentries: Seq[Ward] = _sentries.toMap.values.toSeq

  private val itemUsages = mutable.Map(
    "item_smoke_of_deceit" -> ListBuffer.empty[(GameTimeState, PlayerId)],
    "item_ward_observer" -> ListBuffer.empty[(GameTimeState, PlayerId)]
  )

  private val _observers = mutable.Map.empty[Int, Ward]
  private val _sentries = mutable.Map.empty[Int, Ward]

  @OnEntityCreated(classPattern = "CDOTA_NPC_Observer_Ward.*")
  private def onWardPlaced(ctx: Context, wardEntity: Entity): Unit = {
    val time = TimeState
    val location = Util.getLocation(wardEntity)
    val owner = PlayerId(wardEntity.get[Int]("m_nPlayerOwnerID").get)
    val isSentry = wardEntity.getDtClass.getDtName.contains("TrueSight")

    val ward = Ward(wardEntity.getHandle, isSentry, location, time, time, owner)

    if (isSentry)
      _sentries(ward.id) = ward
    else
      _observers(ward.id) = ward
  }

  @OnEntityPropertyChanged(classPattern = "CDOTA_NPC_Observer_Ward.*", propertyPattern = "m_lifeState")
  private def onWardEnded(ctx: Context, ward: Entity, fp: FieldPath): Unit = {
    if (ward.getPropertyForFieldPath[Int](fp) != 1) return

    val time = TimeState
    val isSentry = ward.getDtClass.getDtName.contains("TrueSight")
    if (isSentry) {
      val current = _sentries(ward.getHandle)
      _sentries(ward.getHandle) = current.copy(ended = time)
    } else {
      val current = _observers(ward.getHandle)
      _observers(ward.getHandle) = current.copy(ended = time)
    }
  }

  @OnCombatLogEntry
  private def onCombatLogEntry(ctx: Context, cle: CombatLogEntry): Unit = {
    if (isItemUsedOnEnemyVision(cle) && itemUsages.contains(cle.getInflictorName)) {
      val heroProcessor = ctx.getProcessor(classOf[HeroProcessor])
      val entities = ctx.getProcessor(classOf[Entities])

      val playerId = heroProcessor.combatLogNameToPlayerId.get(cle.getAttackerName)
      playerId.foreach(id => {
        itemUsages(cle.getInflictorName).addOne(TimeState, PlayerId(id))
      })
    }
  }

  private def isItemUsedOnEnemyVision(cle: CombatLogEntry) =
    cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_ITEM && cle.isVisibleDire && cle.isVisibleRadiant
}
