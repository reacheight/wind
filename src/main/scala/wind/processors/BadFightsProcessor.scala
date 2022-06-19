package wind.processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import wind.Util
import wind.extensions._
import wind.models.Team.Radiant
import wind.models.{BadFight, Fight, PlayerId}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class BadFightsProcessor(fights: Seq[Fight]) extends EntitiesProcessor {
  def badFights: Seq[BadFight] = _badFights.distinct.toSeq

  private val _badFights: ListBuffer[BadFight] = ListBuffer.empty
  private val candidates = fights
    .filter(_.start.gameTime > 600)
    .filter(_.participants.size >= 4)
    .filter(_.winner.nonEmpty)
    .filter(fight => fight.getParticipants(fight.winner.get).size >= fight.getParticipants(Util.getOppositeTeam(fight.winner.get)).size)

  private val players = Util.PlayerIds.map(PlayerId).toSet
  private val EPS = 0.05
  private val FAR_FROM_FIGHT_DISTANCE = 4000
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
        Util.isAlive(hero) && Util.isVisibleByEnemies(hero) && Util.getDistance(heroLocation, fightLocation) > FAR_FROM_FIGHT_DISTANCE
      }))

    candidates
      .find(fight => math.abs(fight.start.gameTime - gameTimeState.gameTime - CHECK_HEROES_NOT_IN_FIGHT_DIFF) < EPS)
      .foreach(fight => {
        currentFight = Some(fight)
        seenHeroes.clear()

        val loserTeamPredicate: PlayerId => Boolean = if (fight.winner.contains(Radiant)) p => p.id >= 10 else p => p.id < 10
        heroesNotInFight = players
          .diff(fight.participants)
          .filter(loserTeamPredicate)
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
        val seenPlayers = seenHeroes.map(Entities.getByHandle).filter(Util.isAlive).map(Util.getPlayerId)
        BadFight(fight, seenPlayers.toSet)
      })
      .filter(_.seenPlayers.nonEmpty)
      .filter(fight => {
        val winner = fight.fight.winner.get
        val loser = Util.getOppositeTeam(winner)
        val aliveLosers = Entities.filter(e => Util.isHero(e) && Util.getTeam(e) == loser && Util.isAlive(e))
        fight.fight.getParticipants(winner).size >= aliveLosers.length - fight.seenPlayers.size
      })
      .map(fight => _badFights.addOne(fight))
  }
}
