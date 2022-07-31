package windota.processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.runner.Context
import windota.Util
import windota.extensions._
import windota.models.Lane._
import windota.models.Team._
import windota.models._

class BlockedCampsProcessor extends EntitiesProcessor {
  val _notUnblockedCamps = collection.mutable.Map.empty[Team, Map[Lane, Seq[Ward]]]
  def notUnblockedCamps: Map[Team, Map[Lane, Seq[Ward]]] = _notUnblockedCamps.toMap

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_nGameState")
  def onGameEnded(ctx: Context, gameRules: Entity, fp: FieldPath): Unit = {
    val gameState = gameRules.getPropertyForFieldPath[Int](fp)
    if (gameState != 6) return

    val visionProcessor = ctx.getProcessor(classOf[VisionProcessor])
    val fullDurationLaneStageWards = visionProcessor.observers.appendedAll(visionProcessor.sentries)
      .filter(ward => ward.created.gameTime < 600 && ward.isFullDuration)

    val (radiantFullDurationWards, direFullDurationWards) = fullDurationLaneStageWards.partition(ward => Util.RadiantPlayerIds.contains(ward.owner))

    val radiantUnblockedCamps = Map(
      Bot -> direFullDurationWards.filter(ward => isInsideBottomEasyCamp(ward.location)),
      Top -> direFullDurationWards.filter(ward => isInsideTopHardCamp(ward.location))
    )

    val direUnblockedCamps = Map(
      Bot -> radiantFullDurationWards.filter(ward => isInsideBottomHardCamp(ward.location)),
      Top -> radiantFullDurationWards.filter(ward => isInsideTopEasyCamp(ward.location))
    )

    _notUnblockedCamps.addOne(Radiant, radiantUnblockedCamps)
    _notUnblockedCamps.addOne(Dire, direUnblockedCamps)
  }

  // checked with CurrentWorldProcessor
  def isInsideBottomEasyCamp(location: Location) =
    location.X >= 11384.875 && location.Y >= 3138.9062 && location.X <= 12155.5 && location.Y <= 3975.8125

  def isInsideBottomHardCamp(location: Location) =
    location.X >= 12281.281 && location.Y >= 4135.5 && location.X <= 13210.3125 && location.Y <= 5053.4062

  def isInsideTopHardCamp(location: Location) =
    location.X >= 3387.8125 && location.Y >= 11458.0625 && location.X <= 4317.5 && location.Y <= 12358.406

  def isInsideTopEasyCamp(location: Location) =
    location.X >= 4413.3438 && location.Y >= 12728.5625 && location.X <= 5247.0625 && location.Y <= 13445.344
}
