package wind.processors

import skadistats.clarity.event.Insert
import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged, UsesEntities}
import wind.Util
import wind.models.{Fight, GameTimeState, PlayerId}

import scala.collection.mutable.ListBuffer

@UsesEntities
class BadFightsProcessor(fights: Seq[Fight]) {
  def badFights: Seq[GameTimeState] = _badFights.distinct.toSeq

  private val _badFights: ListBuffer[GameTimeState] = ListBuffer.empty

  private val _candidates = fights.filter(f => f.participants.count(_.id <= 4) != f.participants.count(_.id > 4))

  @Insert
  private val entities: Entities = null
  private val players = (0 to 9).map(PlayerId)
  private val EPS = 0.1

  private var preFight = false
  private var playersNotInFight = Set.empty[PlayerId]
  private var seenPlayers = Set.empty[PlayerId]

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_fGameTime")
  def onGameTimeChanged(gameRulesEntity: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameTimeState = Util.getGameTimeState(gameRulesEntity)

    if (preFight) {
      val allHeroes = Util.toList(entities.getAllByPredicate(e => Util.isHero(e)))
      val heroesNotInFight = allHeroes.filter(hero => playersNotInFight.contains(PlayerId(hero.getProperty[Int]("m_iPlayerID"))))
      val seenHeroes = heroesNotInFight
        .filter(Util.isVisibleByEnemies)
        .map(hero => PlayerId(hero.getProperty[Int]("m_iPlayerID")))
        .toSet

      seenPlayers = seenPlayers.union(seenHeroes)
    }

    _candidates
      .find(fight => math.abs(fight.start.gameTime - gameTimeState.gameTime - 5) < EPS)
      .foreach(fight => {
        preFight = true
        seenPlayers = Set.empty

        val isRadiantOutnumbered = fight.participants.count(p => p.id <= 4) < fight.participants.count(p => p.id > 4)
        val isDireOutnumbered = fight.participants.count(p => p.id <= 4) > fight.participants.count(p => p.id > 4)

        playersNotInFight = players
          .filter(id => !fight.participants.contains(id))
          .filter(id => (isRadiantOutnumbered && id.id <= 4) || (isDireOutnumbered && id.id > 4))
          .toSet
      })

    _candidates
      .find(fight => math.abs(fight.start.gameTime - gameTimeState.gameTime) < EPS)
      .map(fight => {
        preFight = false
        fight
      })
      .filter(_ => {
        val allHeroes = Util.toList(entities.getAllByPredicate(e => Util.isHero(e)))
        val seenHeroes = allHeroes.filter(hero => seenPlayers.contains(PlayerId(hero.getProperty[Int]("m_iPlayerID"))))
        seenHeroes.exists(hero => hero.getProperty[Int]("m_lifeState") == 0)
      })
      .foreach(fight => _badFights.addOne(fight.start))
  }
}
