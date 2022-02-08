package wind
package processors

import skadistats.clarity.event.Insert
import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}
import wind.models.Location

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
        playerId -> isOutOfFountain(Util.getLocation(courier))
      }).toMap
    }
  }

  private def isOutOfFountain(l: Location): Boolean = -l.X + 3600 < l.Y && l.Y < -l.X + 29080
}
