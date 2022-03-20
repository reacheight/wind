package wind
package processors

import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.runner.Context
import wind.models.{Location, PlayerId}
import wind.extensions._

class CourierProcessor extends EntitiesProcessor {
  def courierIsOut: Map[PlayerId, (Boolean, Boolean)] = _courierIsOut

  private var _courierIsOut: Map[PlayerId, (Boolean, Boolean)] = Map.empty

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_flGameStartTime")
  def onGameStartTimeChanged(ctx: Context, e: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameTimeState = Util.getGameTimeState(e)

    if (gameTimeState.gameStarted) {
      val heroProcessor = ctx.getProcessor(classOf[HeroProcessor])
      val monkeyKingTeam = heroProcessor.heroName
        .find(_._2.contains("MonkeyKing"))
        .map(_._1)
        .map(heroProcessor.heroHandle(_))
        .map(Entities.getByHandle)
        .map(Util.getTeam)

      val couriers = Entities.getAllByName("CDOTA_Unit_Courier")
      _courierIsOut = couriers.map(courier => {
        val playerId = PlayerId(courier.getProperty[Int]("m_nPlayerOwnerID"))
        playerId -> (isOutOfFountain(Util.getLocation(courier)), monkeyKingTeam.contains(Util.getOppositeTeam(Util.getTeam(courier))))
      }).toMap
    }
  }

  private def isOutOfFountain(l: Location): Boolean = -l.X + 3600 < l.Y && l.Y < -l.X + 29080
}
