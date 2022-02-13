package wind.models

import wind.models.Team.{Dire, Radiant, Team}

case class Fight(start: GameTimeState, location: Location, participants: Set[PlayerId], dead: Set[PlayerId]) {
  val radiantParticipants: Set[PlayerId] = participants.filter(_.id <= 4)
  val direParticipants: Set[PlayerId] = participants.filter(_.id > 4)
  val isOutnumbered: Boolean = radiantParticipants.size != direParticipants.size

  val winner: Option[Team] = {
    val radiantDead = dead.filter(_.id <= 4)
    val direDead = dead.filter(_.id > 4)

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
}
