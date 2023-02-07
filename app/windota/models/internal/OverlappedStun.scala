package windota.models.internal

import windota.models.{GameTimeState, PlayerId}

case class OverlappedStun(time: GameTimeState, target: PlayerId, attacker: PlayerId, overlapTime: Float, stunSourceId: Int, isAbility: Boolean)
