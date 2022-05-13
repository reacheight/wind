package wind.models

case class Observer(id: Int, location: Location, created: GameTimeState, ended: GameTimeState, owner: PlayerId)
