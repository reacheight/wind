package wind
package processors

import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.runner.Context

class ItemStockProcessor {
  // todo: заюзать словарь
  private var radiantMaxSmokeStockStart: Float = -1
  private var direMaxSmokeStockStart: Float = -1

  var radiantMaxSmokeStockDuration: Float = 0
  var direMaxSmokeStockDuration: Float = 0

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_vecItemStockInfo.0010.iStockCount")
  def onRadiantSmokeStockChanged(ctx: Context, gameRules: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val smokeStockCount = gameRules.getPropertyForFieldPath[Int](fp)
    val timeState = Util.getGameTimeState(gameRules)

    if (smokeStockCount == 3) {
      radiantMaxSmokeStockStart = timeState.gameTime
    }

    if (smokeStockCount == 2 && radiantMaxSmokeStockStart > 0) {
      radiantMaxSmokeStockDuration += timeState.gameTime - radiantMaxSmokeStockStart
      radiantMaxSmokeStockStart = -1
    }
  }

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_vecItemStockInfo.0011.iStockCount")
  def onDireSmokeStockChanged(ctx: Context, gameRules: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val smokeStockCount = gameRules.getPropertyForFieldPath[Int](fp)
    val timeState = Util.getGameTimeState(gameRules)

    if (smokeStockCount == 3) {
      direMaxSmokeStockStart = timeState.gameTime
    }

    if (smokeStockCount == 2 && direMaxSmokeStockStart > 0) {
      direMaxSmokeStockDuration += timeState.gameTime - direMaxSmokeStockStart
      direMaxSmokeStockStart = -1
    }
  }
}
