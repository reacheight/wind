package windota.models

import windota.models.Team.{Dire, Radiant, Team}

case class Fight(start: GameTimeState, end: GameTimeState, location: Location, participants: Set[PlayerId], dead: Seq[(PlayerId, GameTimeState)]) {
  val radiantParticipants: Set[PlayerId] = participants.filter(_.id <= 8)
  val direParticipants: Set[PlayerId] = participants.filter(_.id > 8)
  val deadRadiant = dead.filter(_._1.id < 10)
  val deadDire = dead.filter(_._1.id >= 10)
  val isOutnumbered: Boolean = radiantParticipants.size != direParticipants.size

  val winner: Option[Team] = {
    if (deadRadiant.size == deadDire.size)
      None
    else if (deadRadiant.size > deadDire.size)
      Some(Dire)
    else
      Some(Radiant)
  }

  val outnumberedTeam: Option[Team] = {
    if (radiantParticipants.size == direParticipants.size)
      None
    else if (radiantParticipants.size < direParticipants.size)
      Some(Radiant)
    else
      Some(Dire)
  }

  def getParticipants(team: Team) = team match {
    case Radiant => radiantParticipants
    case Dire => direParticipants
  }

  def getDead(team: Team) = team match {
    case Radiant => deadRadiant
    case Dire => deadDire
  }
}
