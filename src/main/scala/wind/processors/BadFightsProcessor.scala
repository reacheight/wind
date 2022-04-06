package wind.processors

import skadistats.clarity.model.{Entity}
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import wind.Util
import wind.models.Team.Radiant
import wind.models.{Fight, GameTimeState, PlayerId}
import wind.extensions._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class BadFightsProcessor(fights: Seq[Fight]) extends EntitiesProcessor {
  def badFights: Seq[GameTimeState] = _badFights.distinct.toSeq

  private val _badFights: ListBuffer[GameTimeState] = ListBuffer.empty
  private val candidates = fights
    .filter(_.start.gameTime > 600)
    .filter(_.isOutnumbered)
    .filter(_.participants.size >= 4)
    .filter(_.winner.nonEmpty)
    .filter(fight => Util.getOppositeTeam(fight.outnumberedTeam.get) == fight.winner.get)

  private val players = Util.PlayerIds.map(PlayerId).toSet
  private val EPS = 0.05
  private val NOT_IN_FIGHT_DISTANCE = 6000
  private val CHECK_HEROES_NOT_IN_FIGHT_DIFF = 10

  private var currentFight: Option[Fight] = None
  private var heroesNotInFight = Set.empty[Int]
  private val seenHeroes = mutable.Set.empty[Int]

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_fGameTime")
  def onGameTimeChanged(gameRulesEntity: Entity, fp: FieldPath): Unit = {
    val gameTimeState = Util.getGameTimeState(gameRulesEntity)

    currentFight.foreach(fight => seenHeroes ++= heroesNotInFight
      .filter(handle => {
        val hero = Entities.getByHandle(handle)
        val heroLocation = Util.getLocation(hero)
        val fightLocation = fight.location
        Util.isVisibleByEnemies(hero) && Util.getDistance(heroLocation, fightLocation) > NOT_IN_FIGHT_DISTANCE
      }))

    candidates
      .find(fight => math.abs(fight.start.gameTime - gameTimeState.gameTime - CHECK_HEROES_NOT_IN_FIGHT_DIFF) < EPS)
      .foreach(fight => {
        currentFight = Some(fight)
        seenHeroes.clear()

        val teamPredicate: PlayerId => Boolean = if (fight.outnumberedTeam.contains(Radiant)) p => p.id <= 4 else p => p.id > 4
        heroesNotInFight = players
          .diff(fight.participants)
          .filter(teamPredicate)
          .flatMap(id => Entities.find(e => Util.isHero(e) && e.getProperty[Int]("m_iPlayerID") == id.id))
          .map(_.getHandle)
      })

    candidates
      .find(fight => math.abs(fight.start.gameTime - gameTimeState.gameTime) < EPS)
      .map(fight => {
        currentFight = None
        fight
      })
      .filter(_ => seenHeroes.exists(handle => Entities.getByHandle(handle).getProperty[Int]("m_lifeState") == 0))
      .foreach(fight => _badFights.addOne(fight.start))
  }
}
