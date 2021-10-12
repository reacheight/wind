package wind
package processors

import Lane.Lane

import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}
import skadistats.clarity.processor.runner.Context

import scala.collection.mutable.ArrayBuffer

class LaneProcessor {
  private val EPS = 0.001f
  private val ITERATION_INTERVAL = 10
  private var currentIteration = 1
  private val LANE_STAGE_END_MINUTE = 10
  private val LANE_STAGE_ITERATION_COUNT = 60 * LANE_STAGE_END_MINUTE / ITERATION_INTERVAL
  private var heroLocationMap = Map[Int, ArrayBuffer[(Float, Float)]]()

  var heroLaneStageLocation: Map[Int, (Float, Float)] = Map()
  var heroLaneMap: Map[Int, Lane] = Map()
  var heroLaneStageExp: Map[Int, Int] = Map()
  var heroLaneStageNetworth: Map[Int, Int] = Map()

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_fGameTime")
  def onGameTimeChanged(ctx: Context, gameRulesEntity: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameTimeState = Util.getGameTimeState(gameRulesEntity)
    if (!gameTimeState.gameStarted || currentIteration > LANE_STAGE_ITERATION_COUNT || currentIteration * ITERATION_INTERVAL - gameTimeState.gameTime > EPS) return

    val entities = ctx.getProcessor(classOf[Entities])
    val heroEntities = entities.getAllByPredicate(Util.isHero)
    heroEntities.forEachRemaining(heroEntity => {
      val playerId = heroEntity.getProperty[Int]("m_iPlayerID")
      val location = Util.getLocation(heroEntity)

      if (heroLocationMap.contains(playerId))
        heroLocationMap(playerId) += location
      else
        heroLocationMap += (playerId -> ArrayBuffer(location))
    })

    if (currentIteration == LANE_STAGE_END_MINUTE) {
      heroLaneStageLocation = heroLocationMap.map(item => {
        val (playerId, locations) = item
        val xs = locations.map(_._1)
        val ys = locations.map(_._2)

        playerId -> (xs.sum / xs.length, ys.sum / ys.length)
      })

      heroLaneMap = heroLaneStageLocation.map(item => {
        val (playerId, location) = item
        playerId -> getLane(location._1, location._2)
      })

      val radiantData = entities.getByDtName("CDOTA_DataRadiant")
      val direData = entities.getByDtName("CDOTA_DataDire")
      (getPlayersExpAndNetworth(radiantData) ++ getPlayersExpAndNetworth(direData)).foreach(item => {
        val (playerId, stats) = item
        val (exp, networth) = stats

        heroLaneStageExp += (playerId -> exp)
        heroLaneStageNetworth += (playerId -> networth)
      })
    }

    currentIteration += 1
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

  private def getLane(x: Float, y: Float): Lane = {
    if (y > 10000 && x < 6000) return Lane.Top

    if (y > 6000 && y < 10000 && x > 6000 && x < 10000) return Lane.Middle

    if (y > 2000 && y < 6000 && x > 4000 && x < 10000) return Lane.RadiantJungle

    if (y > 10000 && y < 14000 && x > 6000 && x < 12000) return Lane.DireJungle

    if (y < 6000 && x > 10000) return Lane.Bot

    Lane.Unknown
  }
}
