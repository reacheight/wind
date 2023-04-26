package windota.processors

import skadistats.clarity.event.Insert
import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.{Entities, UsesEntities}
import skadistats.clarity.processor.reader.OnMessage
import skadistats.clarity.wire.common.proto.NetworkBaseTypes
import windota.extensions.EntitiesExtension
import windota.models.GameTimeState

@UsesEntities
class ProcessorBase {
  private var serverTick = 0
  private val TIME_EPS: Float = 0.001f

  @Insert
  protected val Entities: Entities = null
  protected def Time: Float = serverTick / 29.9999984354f
  protected def TimeStateOption: Option[GameTimeState] = {
    val gameRules = Entities
      .findByName("CDOTAGamerulesProxy")

    if (gameRules.isEmpty)
      return Option(GameTimeState(false, false, 0))

    gameRules
      .map(getGameTimeState)
  }

  protected def TimeState = TimeStateOption.get

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
