package windota.processors

import windota.Util
import windota.models.Role.Role
import windota.models.Team._
import windota.models._

class BlockedCampsProcessor extends ProcessorBase {
  def getUnblockedCamps(allWards: Seq[Ward], roles: Map[PlayerId, Role]) = {
    val fullDurationLaneStageWards = allWards
      .filter(ward => ward.created.gameTime < 600 && ward.isFullDuration)

    val notUnblockedCamps = collection.mutable.Map.empty[PlayerId, Seq[Ward]]

    def addUnblockedWards(locationPred: Function[Location, Boolean], responsibleRole: Role, team: Team): Unit = {
      val wards = fullDurationLaneStageWards.filter(ward => locationPred(ward.location))
      if (wards.nonEmpty) {
        val teamIds = if (team == Radiant) Util.RadiantPlayerIds else Util.DirePlayerIds
        val responsible = roles.find { case (playerId, role) => role == responsibleRole && teamIds.contains(playerId.id) }
        responsible.foreach(p => notUnblockedCamps.addOne(p._1 -> wards))
      }
    }

    addUnblockedWards(isInsideBottomEasyCamp, Role.HardSupport, Team.Radiant)
    addUnblockedWards(isInsideBottomHardCamp, Role.SoftSupport, Team.Dire)
    addUnblockedWards(isInsideTopEasyCamp, Role.HardSupport, Team.Dire)
    addUnblockedWards(isInsideTopHardCamp, Role.SoftSupport, Team.Radiant)

    notUnblockedCamps.toMap
  }

  // checked with CurrentWorldProcessor
  private def isInsideBottomEasyCamp(location: Location) =
    location.X >= 11384.875 && location.Y >= 3138.9062 && location.X <= 12155.5 && location.Y <= 3975.8125

  private def isInsideBottomHardCamp(location: Location) =
    location.X >= 12281.281 && location.Y >= 4135.5 && location.X <= 13210.3125 && location.Y <= 5053.4062

  private def isInsideTopHardCamp(location: Location) =
    location.X >= 3387.8125 && location.Y >= 11458.0625 && location.X <= 4317.5 && location.Y <= 12358.406

  private def isInsideTopEasyCamp(location: Location) =
    location.X >= 4413.3438 && location.Y >= 12728.5625 && location.X <= 5247.0625 && location.Y <= 13445.344
}
