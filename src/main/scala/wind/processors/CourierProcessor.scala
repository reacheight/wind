package wind
package processors

import skadistats.clarity.event.Insert
import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}

class CourierProcessor {
  def courierIsOut: Map[Int, Boolean] = _courierIsOut

  @Insert
  private val entities: Entities = null
  private var _courierIsOut: Map[Int, Boolean] = Map.empty

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_flGameStartTime")
  def onGameStartTimeChanged(e: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameTimeState = Util.getGameTimeState(e)

    if (gameTimeState.gameStarted) {
      val couriers = Util.toList(entities.getAllByDtName("CDOTA_Unit_Courier"))
      _courierIsOut = couriers.map(courier => {
        val playerId = courier.getProperty[Int]("m_nPlayerOwnerID")
        val (x, y) = Util.getLocation(courier)

        playerId -> isOutOfFountain(x, y)
      }).toMap
    }
  }

  private def isOutOfFountain(x: Float, y: Float): Boolean = -x + 3600 < y && y < -x + 29080
}
