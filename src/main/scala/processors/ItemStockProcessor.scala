package wind
package processors

import processors.Team._

import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.OnEntityPropertyChanged

class ItemStockProcessor {
  private var maxSmokeStockStart: Map[Team, Float] = Map(Radiant -> -1, Dire -> -1)

  var maxSmokeStockDuration: Map[Team, Float] = Map(Radiant -> 0, Dire -> 0)

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_nGameState")
  def onGameEnded(gameRules: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameState = gameRules.getPropertyForFieldPath[Int](fp)
    if (gameState != 6) return

    val timeState = Util.getGameTimeState(gameRules)

    if (maxSmokeStockStart(Radiant) > 0)
      incrementMaxSmokeStockDuration(Radiant, timeState.gameTime)

    if (maxSmokeStockStart(Dire) > 0)
      incrementMaxSmokeStockDuration(Dire, timeState.gameTime)
  }

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_vecItemStockInfo.0010.iStockCount")
  def onRadiantSmokeStockChanged(gameRules: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit =
    onSmokeStockChanged(Radiant, gameRules, fp)

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_vecItemStockInfo.0011.iStockCount")
  def onDireSmokeStockChanged(gameRules: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit =
    onSmokeStockChanged(Dire, gameRules, fp)

  def onSmokeStockChanged(team: Team, gameRules: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val smokeStockCount = gameRules.getPropertyForFieldPath[Int](fp)
    val timeState = Util.getGameTimeState(gameRules)

    if (smokeStockCount == 3) {
      maxSmokeStockStart += (team -> timeState.gameTime)
    }

    if (smokeStockCount == 2 && maxSmokeStockStart(team) > 0) {
      incrementMaxSmokeStockDuration(team, timeState.gameTime)
      maxSmokeStockStart += (team -> -1)
    }
  }

  def incrementMaxSmokeStockDuration(team: Team, currentTime: Float): Unit =
    maxSmokeStockDuration += (team -> (maxSmokeStockDuration(team) + currentTime - maxSmokeStockStart(team)))
}
