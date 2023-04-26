package windota.processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import windota.Util
import windota.extensions._
import windota.models.Team._

class ItemStockProcessor extends ProcessorBase {
  private var maxSmokeStockStart: Map[Team, Int] = Map(Radiant -> -1, Dire -> -1)
  private var maxObsStockStart: Map[Team, Int] = Map(Radiant -> -1, Dire -> -1)

  var maxSmokeStockDuration: Map[Team, Int] = Map(Radiant -> 0, Dire -> 0)
  var maxObsStockDuration: Map[Team, Int] = Map(Radiant -> 0, Dire -> 0)

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_nGameState")
  def onGameEnded(gameRules: Entity, fp: FieldPath): Unit = {
    val gameState = gameRules.getPropertyForFieldPath[Int](fp)
    if (gameState != 6) return

    val gameTimeState = GameTimeHelper.State

    if (maxSmokeStockStart(Radiant) > 0)
      incrementMaxSmokeStockDuration(Radiant, gameTimeState.gameTime.toInt)

    if (maxSmokeStockStart(Dire) > 0)
      incrementMaxSmokeStockDuration(Dire, gameTimeState.gameTime.toInt)

    if (maxObsStockStart(Radiant) > 0)
      incrementMaxObsStockDuration(Radiant, gameTimeState.gameTime.toInt)

    if (maxObsStockStart(Dire) > 0)
      incrementMaxObsStockDuration(Dire, gameTimeState.gameTime.toInt)
  }

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_vecItemStockInfo.0010.iStockCount")
  def onRadiantSmokeStockChanged(gameRules: Entity, fp: FieldPath): Unit =
    onSmokeStockChanged(Radiant, gameRules, fp)

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_vecItemStockInfo.0011.iStockCount")
  def onDireSmokeStockChanged(gameRules: Entity, fp: FieldPath): Unit =
    onSmokeStockChanged(Dire, gameRules, fp)

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_vecItemStockInfo.0004.iStockCount")
  def onRadiantObsStockChanged(gameRules: Entity, fp: FieldPath): Unit =
    onObsStockChanged(Radiant, gameRules, fp)

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_vecItemStockInfo.0005.iStockCount")
  def onDireObsStockChanged(gameRules: Entity, fp: FieldPath): Unit =
    onObsStockChanged(Dire, gameRules, fp)

  def onSmokeStockChanged(team: Team, gameRules: Entity, fp: FieldPath): Unit = {
    val smokeStockCount = gameRules.getPropertyForFieldPath[Int](fp)
    val gameTimeState = GameTimeHelper.State

    if (smokeStockCount == 3) {
      maxSmokeStockStart += (team -> gameTimeState.gameTime.toInt)
    }

    if (smokeStockCount == 2 && maxSmokeStockStart(team) > 0) {
      incrementMaxSmokeStockDuration(team, gameTimeState.gameTime.toInt)
      maxSmokeStockStart += (team -> -1)
    }
  }

  def onObsStockChanged(team: Team, gameRules: Entity, fp: FieldPath): Unit = {
    val obsStockCount = gameRules.getPropertyForFieldPath[Int](fp)
    val gameTimeState = GameTimeHelper.State

    if (obsStockCount == 4) {
      maxObsStockStart += (team -> gameTimeState.gameTime.toInt)
    }

    if (obsStockCount == 3 && maxObsStockStart(team) > 0) {
      incrementMaxObsStockDuration(team, gameTimeState.gameTime.toInt)
      maxObsStockStart += (team -> -1)
    }
  }

  def incrementMaxSmokeStockDuration(team: Team, currentTime: Int): Unit =
    maxSmokeStockDuration += (team -> (maxSmokeStockDuration(team) + currentTime - maxSmokeStockStart(team)))

  def incrementMaxObsStockDuration(team: Team, currentTime: Int): Unit =
    maxObsStockDuration += (team -> (maxObsStockDuration(team) + currentTime - maxObsStockStart(team)))
}
