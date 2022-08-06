package windota.processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import windota.Util
import windota.extensions._
import windota.models.Team._
import windota.models._

import scala.collection.mutable.ListBuffer

class UnreasonableDivesProcessor(fights: Seq[Fight]) extends EntitiesProcessor {
  private val EPS = 0.05

  private val candidates = fights
    .filter(_.winner.nonEmpty)
    .filter(fight => fight.getDead(Util.getOppositeTeam(fight.winner.get)).size > 1)
    .filter(f => getT4AreaTeam(f.location).nonEmpty)
    .filter(f => f.winner.get == getT4AreaTeam(f.location).get)

  private val _unreasonableDives = ListBuffer.empty[Fight]
  def unreasonableDives = _unreasonableDives.distinct.toSeq

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_fGameTime")
  def onGameTimeChanged(gameRulesEntity: Entity, fp: FieldPath): Unit = {
    val gameTimeState = Util.getGameTimeState(gameRulesEntity)

    candidates
      .find(fight => math.abs(fight.start.gameTime - gameTimeState.gameTime) < EPS)
      .filter(fight => {
        val winner = fight.winner.get
        val winnerMidT3 = Entities.find(e => Util.isTower(e) && Util.getDistance(getMidT3Location(winner), Util.getLocation(e)) < 1)
        winnerMidT3.nonEmpty
      })
      .foreach(fight => _unreasonableDives.addOne(fight))
  }

  def getT4AreaTeam(location: Location): Option[Team] = {
    if (location.X <= 3200 && location.Y <= 3700)
      return Some(Radiant)

    if (location.X >= 12800 && location.Y >= 12400)
      return Some(Dire)

    None
  }

  def getMidT3Location(team: Team): Location = team match {
    case Radiant => Location(3552, 4048)
    case Dire => Location(12464, 11951)
  }
}
