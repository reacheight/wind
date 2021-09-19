package wind

import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}
import skadistats.clarity.processor.runner.Context

class CourierProcessor {
  var courierOutOfFountain: Map[Int, Boolean] = Map()

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_flGameStartTime")
  def onGameStartTimeChanged(ctx: Context, e: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameTimeState = Util.getGameTimeState(e)

    if (gameTimeState.gameStarted) {
      val courierEntities = ctx.getProcessor(classOf[Entities]).getAllByPredicate(e => e.getDtClass.getDtName.startsWith("CDOTA_Unit_Courier"))
      courierEntities.forEachRemaining(courier => {
        val playerId = courier.getProperty[Int]("m_nPlayerOwnerID")
        val (x, y) = Util.getLocation(courier)

        courierOutOfFountain += (playerId -> isOutOfFountain(x, y))
      })
    }
  }

  private def isOutOfFountain(x: Float, y: Float): Boolean = -x + 3600 < y && y < -x + 29080
}
