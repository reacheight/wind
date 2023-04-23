package windota.processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.reader.OnMessage
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.NetworkBaseTypes
import windota.Util
import windota.extensions._
import windota.models.Lane._
import windota.models.Team._
import windota.models._

class LaneProcessor extends EntitiesProcessor {
  private val Epsilon = 0.001f
  private val IterationInterval = 10
  private val LaneStageEndMinute = 10
  private val LaneStageIterationCount = 60 * LaneStageEndMinute / IterationInterval

  private var currentIteration = 1
  private var heroLocationMap = Map[Int, Array[Location]]()

  var laneStageLocation: Map[Int, (Location, Location)] = Map()
  var playerLane: Map[Int, (Lane, Lane)] = Map()
  var laneExp: Map[Int, Int] = Map()
  var laneNetworth: Map[Int, Int] = Map()
  var laneWinner: Map[Lane, Option[Team]] = Map(Top -> None, Middle -> None, Bot -> None)

  @OnMessage(classOf[NetworkBaseTypes.CNETMsg_Tick])
  def onGameTimeChanged(ctx: Context, message: NetworkBaseTypes.CNETMsg_Tick): Unit = {
    val gameTimeState = TimeState
    if (!gameTimeState.gameStarted || gameTimeState.gameTime < 30 || currentIteration > LaneStageIterationCount || currentIteration * IterationInterval - gameTimeState.gameTime > Epsilon) return

    val heroEntities = Entities.getAllByPredicate(Util.isHero)
    heroEntities.forEachRemaining(heroEntity => {
      val playerId = heroEntity.getProperty[Int]("m_iPlayerID")
      val location = Util.getLocation(heroEntity)

      heroLocationMap += playerId -> (heroLocationMap.getOrElse(playerId, Array.empty) :+ location)
    })

    if (currentIteration == LaneStageIterationCount)
      onLaneStageEnded()

    currentIteration += 1
  }

  def onLaneStageEnded(): Unit = {
    laneStageLocation = heroLocationMap map {case (playerId, locations) =>
      val (firstHalf, secondHalf) = locations.splitAt(locations.length / 2)
      val firstHalfLocation = Location(firstHalf.map(_.X).sum / firstHalf.length, firstHalf.map(_.Y).sum / firstHalf.length)
      val secondHalfLocation = Location(secondHalf.map(_.X).sum / secondHalf.length, secondHalf.map(_.Y).sum / secondHalf.length)

      playerId -> (firstHalfLocation, secondHalfLocation)
    }

    playerLane = laneStageLocation map {case (playerId, (firstStageLocation, secondStageLocation)) =>
      playerId -> (Util.getLane(firstStageLocation), Util.getLane(secondStageLocation))
    }

    val radiantData = Entities.getByDtName("CDOTA_DataRadiant")
    val direData = Entities.getByDtName("CDOTA_DataDire")
    (Util.getPlayersExpAndNetworth(radiantData) ++ Util.getPlayersExpAndNetworth(direData)) foreach {case (playerId, (exp, networth)) =>
      laneExp += (playerId -> exp)
      laneNetworth += (playerId -> networth)
    }

    for (lane <- List(Top, Middle, Bot))
      laneWinner += lane -> getLaneWinner(lane)
  }

  private def getLaneWinner(lane: Lane): Option[Team] = {
    val lanePlayers = playerLane.filter(_._2._1 == lane).keys
    val (radiantPlayers, direPlayers) = lanePlayers.partition(_ < 5)
    if (radiantPlayers.isEmpty || direPlayers.isEmpty)
      return None

    val radiantScore = radiantPlayers.map(id => laneExp(id) + laneNetworth(id)).sum / radiantPlayers.size
    val direScore = direPlayers.map(id => laneExp(id) + laneNetworth(id)).sum / direPlayers.size

    radiantScore - direScore match {
      case score if score > 1000 => Some(Radiant)
      case score if score < -1000 => Some(Dire)
      case _ => None
    }
  }
}
