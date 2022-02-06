package wind.processors

import skadistats.clarity.event.Insert
import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged, UsesEntities}
import wind.Util
import wind.models.GameTimeState

import scala.collection.mutable.ListBuffer

@UsesEntities
class FightProcessor {
  def fights: Seq[(GameTimeState, (Float, Float))] = _fights

  private val _deaths: ListBuffer[(GameTimeState, (Float, Float))] = ListBuffer.empty
  private var _fights: Seq[(GameTimeState, (Float, Float))] = Seq.empty

  private val TIME_DISTANCE = 20
  private val LOCATION_DISTANCE = 3000

  @Insert
  private val entities: Entities = null

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_nGameState")
  def onGameEnded(gameRules: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameState = gameRules.getPropertyForFieldPath[Int](fp)
    if (gameState != 6) return

    val splitByLocation = _deaths.foldLeft(ListBuffer.empty[ListBuffer[(GameTimeState, (Float, Float))]]) { case (locations, (deathTime, deathLocation)) =>
      locations.find(location => {
        val averageLocation = Util.getAverageLocation(location.map(_._2).toSeq)
        val distance = Util.getDistance(averageLocation, deathLocation)

        distance < LOCATION_DISTANCE
      }) match {
        case Some(location) => location.addOne((deathTime, deathLocation))
        case None => locations.addOne(ListBuffer((deathTime, deathLocation)))
      }

      locations
    }

    _fights = splitByLocation.flatMap(deaths =>
      deaths.foldLeft(Seq(Seq.empty[(GameTimeState, (Float, Float))])) { case (fights, (deathTime, location)) =>
        val curFight = fights.head
        val prevFights = fights.tail
        if (curFight.isEmpty || deathTime.gameTime - curFight.last._1.gameTime <= TIME_DISTANCE)
          (curFight :+ (deathTime, location)) +: prevFights
        else
          Seq((deathTime, location)) +: fights
      }
        .filter(_.length >= 2)
        .map(fight => (fight.head._1, Util.getAverageLocation(fight.map(_._2)))))
      .sortBy(_._1.gameTime)
      .toSeq
  }

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(hero: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    if (!Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 1) return

    val gameRules = entities.getByDtName("CDOTAGamerulesProxy")
    val time = Util.getGameTimeState(gameRules)

    _deaths.addOne((time, Util.getLocation(hero)))
  }
}
