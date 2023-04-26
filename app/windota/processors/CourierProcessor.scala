package windota.processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.runner.Context
import windota.Util
import windota.extensions._
import windota.models._

class CourierProcessor extends ProcessorBase {
  def courierIsOut: Map[PlayerId, (Boolean, Boolean)] = _courierIsOut

  private var _courierIsOut: Map[PlayerId, (Boolean, Boolean)] = Map.empty

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_flGameStartTime")
  def onGameStartTimeChanged(ctx: Context, e: Entity, fp: FieldPath): Unit = {
    val gameTimeState = TimeState

    if (gameTimeState.gameStarted) {
      val monkeyKingTeam = Entities.findByName("CDOTA_Unit_Hero_MonkeyKing")
        .find(Util.isHero)
        .map(Util.getTeam)

      val couriers = Entities.filterByName("CDOTA_Unit_Courier")
      _courierIsOut = couriers.map(courier => {
        val playerId = PlayerId(courier.getProperty[Int]("m_nPlayerOwnerID"))
        playerId -> (isOutOfFountain(Util.getLocation(courier)), monkeyKingTeam.contains(Util.getOppositeTeam(Util.getTeam(courier))))
      }).toMap
    }
  }

  private def isOutOfFountain(l: Location): Boolean = -l.X + 3600 < l.Y && l.Y < -l.X + 29080
}
