package windota.models

import windota.models.Team.{Dire, Radiant, Team}

case class Fight(start: GameTimeState, end: GameTimeState, location: Location, participants: Set[PlayerId], dead: Set[PlayerId]) {
  val radiantParticipants: Set[PlayerId] = participants.filter(_.id <= 8)
  val direParticipants: Set[PlayerId] = participants.filter(_.id > 8)
  val deadRadiant = dead.filter(_.id <= 8)
  val deadDire = dead.filter(_.id <= 8)
  val isOutnumbered: Boolean = radiantParticipants.size != direParticipants.size

  val winner: Option[Team] = {
    val radiantDead = dead.filter(_.id <= 8)
    val direDead = dead.filter(_.id > 8)

    if (radiantDead.size == direDead.size)
      None
    else if (radiantDead.size > direDead.size)
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