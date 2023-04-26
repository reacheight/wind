package windota.processors.helpers

import skadistats.clarity.event.Insert
import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.Entities
import skadistats.clarity.processor.reader.OnMessage
import skadistats.clarity.wire.common.proto.NetworkBaseTypes
import windota.extensions.EntitiesExtension
import windota.models.GameTimeState

class GameTimeHelperProcessor {
  private var serverTick = 0
  private val TIME_EPS: Float = 0.001f

  @Insert
  private val Entities: Entities = null
  private def Time: Float = serverTick / 29.9999984354f

  def State: GameTimeState = {
    val gameRulesOpt = Entities
      .findByName("CDOTAGamerulesProxy")

    gameRulesOpt match {
      case None => GameTimeState(preGameStarted = false, gameStarted = false, gameTime = Float.MinValue)
      case Some(gameRules) => getGameTimeState(gameRules)
    }
  }

  private def getGameTimeState(gameRulesEntity: Entity): GameTimeState = {
    if (gameRulesEntity.getDtClass.getDtName != "CDOTAGamerulesProxy") throw new IllegalArgumentException

    if (Time > TIME_EPS) {
      val preGameTime = gameRulesEntity.getProperty[Float]("m_pGameRules.m_flPreGameStartTime")

      if (preGameTime > TIME_EPS) {
        val startTime = gameRulesEntity.getProperty[Float]("m_pGameRules.m_flGameStartTime")
        if (startTime > TIME_EPS) {
          return GameTimeState(preGameStarted = true, gameStarted = true, gameTime = Time - startTime)
        }
        else {
          val transitionTime = gameRulesEntity.getProperty[Float]("m_pGameRules.m_flStateTransitionTime")
          return GameTimeState(preGameStarted = true, gameStarted = false, gameTime = Time - transitionTime)
        }
      }

      return GameTimeState(preGameStarted = false, gameStarted = false, gameTime = Float.MinValue)
    }

    GameTimeState(preGameStarted = false, gameStarted = false, gameTime = Float.MinValue)
  }

  @OnMessage(classOf[NetworkBaseTypes.CNETMsg_Tick])
  private def onMessage(message: NetworkBaseTypes.CNETMsg_Tick): Unit = {
    serverTick = message.getTick
  }
}
