package windota.processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.reader.OnMessage
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.NetworkBaseTypes
import windota.Util
import windota.Util._
import windota.extensions.{EntitiesExtension, FieldPath}
import windota.models.Lane.Lane
import windota.models._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class UnreactedLaneGanksProcessor(fights: Seq[Fight], playerLanes: Map[PlayerId, Lane]) extends ProcessorBase {
  private val EPS = 0.05
  private val LANE_GANK_CHECK_RANGE = 2500
  private val START_CHECK_TIME_BEFORE_GANK = 30
  private val STOP_CHECK_TIME_BEFORE_GANK = 5
  private val LANE_STAGE_END = 12 * 60
  private val lanes = Seq(Lane.Bot, Lane.Top, Lane.Middle)


  private val laneGanks = fights
    .filter(f => f.start.gameTime < LANE_STAGE_END)
    .filter(f => lanes.contains(Util.getLane(f.location)))
    .map(fight => {
      val fightLane = Util.getLane(fight.location)
      val laners = playerLanes.filter { case (_, lane) => fightLane == lane }.keys.toList

      val (radiantLaners, direLaners) = laners.partition(_.id < 10)

      val allRadiantParticipantsAreLaners = fight.radiantParticipants.forall(p => radiantLaners.contains(p))
      val allDireParticipantsAreLaners = fight.direParticipants.forall(p => direLaners.contains(p))

      val direGankOnRadiant = allRadiantParticipantsAreLaners && !allDireParticipantsAreLaners
      val radiantGankOnDire = !allRadiantParticipantsAreLaners && allDireParticipantsAreLaners
      (fight, direGankOnRadiant, radiantGankOnDire)
    })
    .filter(tuple => tuple._2 || tuple._3)
    .map(tuple => (tuple._1, if (tuple._2) Team.Dire else Team.Radiant))

  private var currentGank: Option[Fight] = None
  private val gankers = mutable.Set.empty[Int]           // Int - handlers
  private val seenGankers = mutable.Set.empty[Int]      // Int - handlers

  private val _unreactedLaneGanks: ListBuffer[(PlayerId, Seq[PlayerId], GameTimeState, Lane)] = ListBuffer.empty
  def unreactedLaneGanks = _unreactedLaneGanks.toSeq

  @OnMessage(classOf[NetworkBaseTypes.CNETMsg_Tick])
  def onGameTimeChanged(ctx: Context, message: NetworkBaseTypes.CNETMsg_Tick): Unit = {
    val gameTimeState = GameTimeHelper.State

    currentGank.foreach(fight => seenGankers ++= gankers
      .filter(handle => {
        val ganker = Entities.getByHandle(handle)
        val gankerLocation = Util.getLocation(ganker)
        val gankLocation = fight.location

        Util.isVisibleByEnemies(ganker) && Util.getDistance(gankerLocation, gankLocation) <= LANE_GANK_CHECK_RANGE
      }))

    laneGanks
      .find(gank => math.abs(gank._1.start.gameTime - gameTimeState.gameTime - START_CHECK_TIME_BEFORE_GANK) < EPS)
      .foreach { case (fight, gankingTeam) =>
        currentGank = Some(fight)
        seenGankers.clear()
        gankers.clear()

        val fightLane = Util.getLane(fight.location)
        gankers ++= fight.getParticipants(gankingTeam).filter(id => playerLanes(id) != fightLane)
          .flatMap(id => Entities.find(e => e.isHero && e.playerId == id))
          .map(_.getHandle)
      }

    laneGanks
      .find(gank => math.abs(gank._1.start.gameTime - gameTimeState.gameTime - STOP_CHECK_TIME_BEFORE_GANK) < EPS)
      .map(gank => {
        currentGank = None
        gank
      })
      .foreach { case (fight, gankingTeam) =>
        val gankLane = Util.getLane(fight.location)
        val gankerPlayerIds = seenGankers.map(Entities.getByHandle).map(_.playerId).toSeq
        if (gankerPlayerIds.size == gankers.size)
          fight.getDead(Util.getOppositeTeam(gankingTeam))
            .foreach(id => _unreactedLaneGanks.addOne((id, gankerPlayerIds, fight.start, gankLane)))
      }
  }
}
