package windota.processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import windota.Util
import windota.Util._
import windota.extensions._
import windota.models.Team._
import windota.models._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class BadFightsProcessor(fights: Seq[Fight]) extends EntitiesProcessor {
  def badFights: Seq[BadFight] = _badFights.distinct.toSeq

  private val _badFights: ListBuffer[BadFight] = ListBuffer.empty
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
  private val seenHeroes2 = mutable.Map.empty[Int, Location]

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_fGameTime")
  def onGameTimeChanged(gameRulesEntity: Entity, fp: FieldPath): Unit = {
    val gameTimeState = Util.getGameTimeState(gameRulesEntity)

    currentFight.foreach(fight => seenHeroes2 ++= heroesNotInFight
      .flatMap(handle => {
        val hero = Entities.getByHandle(handle)
        val heroLocation = Util.getLocation(hero)
        val fightLocation = fight.location
        if (Util.isVisibleByEnemies(hero) && Util.getDistance(heroLocation, fightLocation) > NOT_IN_FIGHT_DISTANCE)
          Some(handle -> heroLocation)
        else
          None
      }))

    candidates
      .find(fight => math.abs(fight.start.gameTime - gameTimeState.gameTime - CHECK_HEROES_NOT_IN_FIGHT_DIFF) < EPS)
      .foreach(fight => {
        currentFight = Some(fight)
        seenHeroes2.clear()

        val teamPredicate: PlayerId => Boolean = if (fight.outnumberedTeam.contains(Radiant)) p => p.id < 10 else p => p.id >= 10
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
      .map(fight => {
        val seenPlayers = seenHeroes2.map { case (handle, location) => (Entities.getByHandle(handle), location) }
          .filter(_._1.isActive)
          .map { case (hero, location) => (hero.playerId, location)}
        BadFight(fight, seenPlayers.toMap)
      })
      .filter(_.seenPlayers.nonEmpty)
      .filter(fight => fight.fight.getParticipants(fight.fight.winner.get).size > 5 - fight.seenPlayers.size)
      .map(fight => _badFights.addOne(fight))
  }
}
