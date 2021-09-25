package wind

import skadistats.clarity.event.Insert
import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}

class CourierProcessor {
  var courierOutOfFountain: Map[Int, Boolean] = Map()

  @Insert
  private val entities: Entities = null

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_flGameStartTime")
  def onGameStartTimeChanged(e: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameTimeState = Util.getGameTimeState(e)

    if (gameTimeState.gameStarted) {
      val courierEntities = entities.getAllByPredicate(e => e.getDtClass.getDtName.startsWith("CDOTA_Unit_Courier"))
      courierEntities.forEachRemaining(courier => {
        val playerId = courier.getProperty[Int]("m_nPlayerOwnerID")
        val (x, y) = Util.getLocation(courier)

        courierOutOfFountain += (playerId -> isOutOfFountain(x, y))
      })
    }
  }

  private def isOutOfFountain(x: Float, y: Float): Boolean = -x + 3600 < y && y < -x + 29080
}
