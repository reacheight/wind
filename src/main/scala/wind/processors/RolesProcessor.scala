package wind.processors

import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}
import skadistats.clarity.processor.runner.Context
import wind.Util
import wind.models.Lane.{Bot, Lane, Middle, Top}
import wind.models.PlayerId
import wind.models.Role.{HardSupport, MidLane, OffLane, Role, SafeLane, SoftSupport, Undefined}

class RolesProcessor {
  def roles: Map[PlayerId, Role] = _roles
  private var _roles: Map[PlayerId, Role] = Map.empty

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_nGameState")
  def onGameEnded(ctx: Context, gameRules: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameState = gameRules.getPropertyForFieldPath[Int](fp)
    if (gameState != 6) return

    val entities = ctx.getProcessor(classOf[Entities])
    val radiantData = entities.getByDtName("CDOTA_DataRadiant")
    val direData = entities.getByDtName("CDOTA_DataDire")

    val radiantNetworth = Util.getPlayersExpAndNetworth(radiantData).map { case (id, (_, networth)) => id -> networth }
    val direNetworth = Util.getPlayersExpAndNetworth(direData).map { case (id, (_, networth)) => id -> networth }

    val laneProcessor = ctx.getProcessor(classOf[LaneProcessor])
    val radiantRoles = Util.RadiantPlayerIds
      .map(id => {
        val lane = laneProcessor.playerLane(id)._1
        PlayerId(id) -> getRadiantRole(isCore(id, radiantNetworth), lane)
      })

    val direRoles = Util.DirePlayerIds
      .map(id => {
        val lane = laneProcessor.playerLane(id)._1
        PlayerId(id) -> getDireRole(isCore(id, direNetworth), lane)
      })

    _roles = (radiantRoles ++ direRoles).toMap
  }

  private def getRadiantRole(isCore: Boolean, lane: Lane): Role = lane match {
    case Bot if isCore => SafeLane
    case Bot if !isCore => HardSupport
    case Middle if isCore => MidLane
    case Top if isCore => OffLane
    case Top if !isCore => SoftSupport
    case _ => Undefined
  }

  private def getDireRole(isCore: Boolean, lane: Lane): Role = lane match {
    case Bot if isCore => OffLane
    case Bot if !isCore => SoftSupport
    case Middle if isCore => MidLane
    case Top if isCore => SafeLane
    case Top if !isCore => HardSupport
    case _ => Undefined
  }

  private def isCore(playerId: Int, teamNetworth: Map[Int, Int]): Boolean =
    teamNetworth.toSeq.sortBy(_._2).indexWhere(_._1 == playerId) > 1
}
