package wind

import skadistats.clarity.model.Entity

object Util {
  private val TIME_EPS: Float = 0.001f

  def getGameTimeState(gameRulesEntity: Entity): GameTimeState = {
    if (gameRulesEntity.getDtClass.getDtName != "CDOTAGamerulesProxy") throw new IllegalArgumentException

    val gameTime : Float = gameRulesEntity.getProperty("m_pGameRules.m_fGameTime")
    if (gameTime > TIME_EPS) {
      val preGameTime = gameRulesEntity.getProperty[Float]("m_pGameRules.m_flPreGameStartTime")

      if (preGameTime > TIME_EPS){
        val startTime = gameRulesEntity.getProperty[Float]("m_pGameRules.m_flGameStartTime")
        if (startTime > TIME_EPS) {
          return new GameTimeState(true, true, gameTime - startTime)
        }
        else {
          val transitionTime = gameRulesEntity.getProperty[Float]("m_pGameRules.m_flStateTransitionTime")
          return new GameTimeState(true, false, gameTime - transitionTime)
        }
      }

      return new GameTimeState(false, false, Float.MinValue)
    }

    new GameTimeState(false, false, Float.MinValue)
  }
}

class GameTimeState(val preGameStarted: Boolean, val gameStarted: Boolean, val gameTime: Float)
