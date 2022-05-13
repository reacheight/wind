package wind.models

import wind.models.Team._

case class FightUnderVision(fight: Fight, observers: Seq[Observer]) {
  def  getTeamWards(team: Team) = team match {
    case Radiant => observers.filter(_.owner.id < 10)
    case Dire => observers.filter(_.owner.id >= 10)
  }
}
