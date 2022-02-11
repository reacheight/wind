package wind.models

case class Fight(start: GameTimeState, location: Location, participants: Seq[PlayerId])
