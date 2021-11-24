package wind
package processors

import Lane._
import Team._
import skadistats.clarity.event.Insert
import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}

class LaneProcessor {
  private val Epsilon = 0.001f
  private val IterationInterval = 10
  private val LaneStageEndMinute = 10
  private val LaneStageIterationCount = 60 * LaneStageEndMinute / IterationInterval

  @Insert
  private val entities: Entities = null

  private var currentIteration = 1
  private var heroLocationMap = Map[Int, Array[(Float, Float)]]()

  var laneStageLocation: Map[Int, ((Float, Float), (Float, Float))] = Map()
  var playerLane: Map[Int, (Lane, Lane)] = Map()
  var laneExp: Map[Int, Int] = Map()
  var laneNetworth: Map[Int, Int] = Map()
  var laneWinner: Map[Lane, Option[Team]] = Map(Top -> None, Middle -> None, Bot -> None)

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_fGameTime")
  def onGameTimeChanged(gameRulesEntity: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameTimeState = Util.getGameTimeState(gameRulesEntity)
    if (!gameTimeState.gameStarted || gameTimeState.gameTime < 30 || currentIteration > LaneStageIterationCount || currentIteration * IterationInterval - gameTimeState.gameTime > Epsilon) return

    val heroEntities = entities.getAllByPredicate(Util.isHero)
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
      val firstHalfLocation = (firstHalf.map(_._1).sum / firstHalf.length, firstHalf.map(_._2).sum / firstHalf.length)
      val secondHalfLocation = (secondHalf.map(_._1).sum / secondHalf.length, secondHalf.map(_._2).sum / secondHalf.length)

      playerId -> (firstHalfLocation, secondHalfLocation)
    }

    playerLane = laneStageLocation map {case (playerId, (firstStageLocation, secondStageLocation)) =>
      playerId -> ((getLane _).tupled(firstStageLocation), (getLane _).tupled(secondStageLocation))
    }

    val radiantData = entities.getByDtName("CDOTA_DataRadiant")
    val direData = entities.getByDtName("CDOTA_DataDire")
    (getPlayersExpAndNetworth(radiantData) ++ getPlayersExpAndNetworth(direData)) foreach {case (playerId, (exp, networth)) =>
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

  private def getPlayersExpAndNetworth(data: Entity): Map[Int, (Int, Int)] = {
    val isRadiant = data.getDtClass.getDtName == "CDOTA_DataRadiant"

    (0 to 4).map(playerNumber => {
      val playerId = if (isRadiant) playerNumber else playerNumber + 5
      val propertyPrefix = s"m_vecDataTeam.000$playerNumber."
      val exp = data.getProperty[Int](propertyPrefix + "m_iTotalEarnedXP")
      val networth = data.getProperty[Int](propertyPrefix + "m_iNetWorth")

      playerId -> (exp, networth)
    }).toMap
  }

  private def getLane(x: Float, y: Float): Lane = (x, y) match {
      case _ if y > 10000 && x < 4500 => Lane.Top
      case _ if y > 6000 && y < 10000 && x > 6000 && x < 10000 => Lane.Middle
      case _ if y > 2000 && y < 6000 && x > 4000 && x < 11800 => Lane.RadiantJungle
      case _ if y > 10000 && y < 14000 && x > 4500 && x < 12000 => Lane.DireJungle
      case _ if y < 6000 && x > 11800 => Lane.Bot
      case _ => Lane.Roaming
    }
}
