package wind.processors

import skadistats.clarity.event.Insert
import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged, UsesEntities}
import wind.Util
import wind.models.{Fight, GameTimeState, Location, PlayerId}

import scala.collection.mutable.ListBuffer

@UsesEntities
class FightProcessor {
  type DeathData = (GameTimeState, Location, Map[PlayerId, Location])

  def fights: Seq[Fight] = _fights

  private val _deaths: ListBuffer[DeathData] = ListBuffer.empty
  private var _fights: Seq[Fight] = Seq.empty

  private val TIME_DISTANCE = 20
  private val FIGHT_LOCATION_DISTANCE = 4000
  private val HERO_IN_FIGHT_DISTANCE = 2000

  @Insert
  private val entities: Entities = null

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_nGameState")
  def onGameEnded(gameRules: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameState = gameRules.getPropertyForFieldPath[Int](fp)
    if (gameState != 6) return

    val splitByLocation = _deaths.foldLeft(ListBuffer.empty[ListBuffer[DeathData]]) { case (locations, (deathTime, deathLocation, heroLocations)) =>
      locations.find(location => {
        val averageLocation = Util.getAverageLocation(location.map(_._2).toSeq)
        val distance = Util.getDistance(averageLocation, deathLocation)

        distance < FIGHT_LOCATION_DISTANCE
      }) match {
        case Some(location) => location.addOne((deathTime, deathLocation, heroLocations))
        case None => locations.addOne(ListBuffer((deathTime, deathLocation, heroLocations)))
      }

      locations
    }

    val fights = splitByLocation.flatMap(deaths => deaths.foldLeft(Seq(Seq.empty[DeathData])) { case (fights, (deathTime, location, heroLocations)) =>
      val curFight = fights.head
      val prevFights = fights.tail
      if (curFight.isEmpty || deathTime.gameTime - curFight.last._1.gameTime <= TIME_DISTANCE)
        (curFight :+ (deathTime, location, heroLocations)) +: prevFights
      else
        Seq((deathTime, location, heroLocations)) +: fights
    })
      .filter(_.length >= 2)

    _fights = fights.map(deaths => {
      val start = deaths.head._1
      val fightLocation = Util.getAverageLocation(deaths.map(_._2))

      val heroesLocations = deaths.flatMap(_._3)
      val heroesInFight = heroesLocations
        .filter { case (_, location) => Util.getDistance(location, fightLocation) < HERO_IN_FIGHT_DISTANCE }
        .map(_._1)
        .toSet

      (start, fightLocation, heroesInFight)
    })
      .sortBy(_._1.gameTime)
      .map(fight => Fight(fight._1, fight._2, fight._3))
      .toSeq
  }

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(hero: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    if (!Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 1) return

    val gameRules = entities.getByDtName("CDOTAGamerulesProxy")
    val time = Util.getGameTimeState(gameRules)

    val heroes = Util.toList(entities.getAllByPredicate(Util.isHero))
    val locations = heroes.map(hero => PlayerId(hero.getProperty[Int]("m_iPlayerID")) -> Util.getLocation(hero)).toMap

    _deaths.addOne((time, Util.getLocation(hero), locations))
  }
}
