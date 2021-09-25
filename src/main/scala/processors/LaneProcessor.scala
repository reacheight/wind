package wind
package processors

import Lane.Lane

import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}
import skadistats.clarity.processor.runner.Context

import scala.collection.mutable.ArrayBuffer

class LaneProcessor {
  private val EPS = 0.001f
  private val LANE_STAGE_END_MINUTE = 7
  private var currentMin = 1
  private var heroLocationMap = Map[Int, ArrayBuffer[(Float, Float)]]()

  var heroLaneStageLocation: Map[Int, (Float, Float)] = Map()
  var heroLaneMap: Map[Int, Lane] = Map()

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_fGameTime")
  def onGameTimeChanged(ctx: Context, gameRulesEntity: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameTimeState = Util.getGameTimeState(gameRulesEntity)
    if (!gameTimeState.gameStarted || currentMin > LANE_STAGE_END_MINUTE || currentMin * 60 - gameTimeState.gameTime > EPS) return

    val heroEntities = ctx.getProcessor(classOf[Entities]).getAllByPredicate(Util.isHero)
    heroEntities.forEachRemaining(heroEntity => {
      val playerId = heroEntity.getProperty[Int]("m_iPlayerID")
      val location = Util.getLocation(heroEntity)

      if (heroLocationMap.contains(playerId))
        heroLocationMap(playerId) += location
      else
        heroLocationMap += (playerId -> ArrayBuffer(location))
    })

    if (currentMin == LANE_STAGE_END_MINUTE) {
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
    }

    currentMin += 1
  }

  private def getLane(x: Float, y: Float): Lane = {
    if (y > 10000 && x < 6000) return Lane.RadiantTop

    if (y > 6000 && y < 10000 && x > 6000 && x < 10000) return Lane.Middle

    if (y < 6000 && x > 10000) return Lane.RadiantBot

    Lane.Unknown
  }
}
