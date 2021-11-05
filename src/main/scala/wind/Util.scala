package wind

import skadistats.clarity.model.Entity

object Util {
  private val TIME_EPS: Float = 0.001f
  private val nullValue = 16777215
  private val replicatingPropertyName = "m_hReplicatingOtherHeroModel"

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

  def isHero(entity: Entity): Boolean =
    entity.getDtClass.getDtName.startsWith("CDOTA_Unit_Hero") &&
      entity.hasProperty(replicatingPropertyName) &&
      entity.getProperty[Int](replicatingPropertyName) == nullValue

  def getLocation(entity: Entity): (Float, Float) = {
    if (!entity.hasProperty("CBodyComponent.m_cellX") || !entity.hasProperty("CBodyComponent.m_cellY") ||
      !entity.hasProperty("CBodyComponent.m_vecX") || !entity.hasProperty("CBodyComponent.m_vecY")) {
      throw new IllegalArgumentException
    }

    val (x, y) = (entity.getProperty[Int]("CBodyComponent.m_cellX"), entity.getProperty[Int]("CBodyComponent.m_cellY"))
    val (vecX, vecY) = (entity.getProperty[Float]("CBodyComponent.m_vecX"), entity.getProperty[Float]("CBodyComponent.m_vecY"))

    (x * 128 + vecX - 8192, y * 128 + vecY - 8192)
  }
}

class GameTimeState(val preGameStarted: Boolean, val gameStarted: Boolean, val gameTime: Float)
