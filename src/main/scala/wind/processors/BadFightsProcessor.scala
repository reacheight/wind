package wind.processors

import skadistats.clarity.event.Insert
import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged, UsesEntities}
import wind.Util
import wind.models.Team.Radiant
import wind.models.{Fight, GameTimeState, PlayerId}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

@UsesEntities
class BadFightsProcessor(fights: Seq[Fight]) {
  def badFights: Seq[GameTimeState] = _badFights.distinct.toSeq

  private val _badFights: ListBuffer[GameTimeState] = ListBuffer.empty
  private val candidates = fights
    .filter(_.isOutnumbered)
    .filter(_.participants.size >= 4)
    .filter(_.winner.nonEmpty)
    .filter(fight => Util.getOppositeTeam(fight.outnumberedTeam.get) == fight.winner.get)

  @Insert
  private val entities: Entities = null
  private val players = (0 to 9).map(PlayerId).toSet
  private val EPS = 0.05

  private var preFight = false
  private var heroesNotInFight = Set.empty[Int]
  private val seenHeroes = mutable.Set.empty[Int]

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_fGameTime")
  def onGameTimeChanged(gameRulesEntity: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameTimeState = Util.getGameTimeState(gameRulesEntity)

    if (preFight)
      seenHeroes ++= heroesNotInFight.filter(handle => Util.isVisibleByEnemies(entities.getByHandle(handle)))

    candidates
      .find(fight => math.abs(fight.start.gameTime - gameTimeState.gameTime - 5) < EPS)
      .foreach(fight => {
        preFight = true
        seenHeroes.clear()

        val teamPredicate: PlayerId => Boolean = if (fight.outnumberedTeam.contains(Radiant)) p => p.id <= 4 else p => p.id > 4
        heroesNotInFight = players
          .diff(fight.participants)
          .filter(teamPredicate)
          .map(id => entities.getByPredicate(e => Util.isHero(e) && e.getProperty[Int]("m_iPlayerID") == id.id).getHandle)
      })

    candidates
      .find(fight => math.abs(fight.start.gameTime - gameTimeState.gameTime) < EPS)
      .map(fight => {
        preFight = false
        fight
      })
      .filter(_ => seenHeroes.exists(handle => entities.getByHandle(handle).getProperty[Int]("m_lifeState") == 0))
      .foreach(fight => _badFights.addOne(fight.start))
  }
}
