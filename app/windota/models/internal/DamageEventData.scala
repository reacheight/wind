package windota.models.internal

import windota.models.{GameTimeState, PlayerId}

case class DamageEventData(target: PlayerId, attacker: PlayerId, amount: Int, time: GameTimeState)
