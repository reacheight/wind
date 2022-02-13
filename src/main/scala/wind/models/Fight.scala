package wind.models

case class Fight(start: GameTimeState, location: Location, participants: Set[PlayerId], dead: Set[PlayerId]) {
  val radiantParticipants: Set[PlayerId] = participants.filter(_.id <= 4)
  val direParticipants: Set[PlayerId] = participants.filter(_.id > 4)
  val isOutnumbered: Boolean = radiantParticipants.size != direParticipants.size
}
