package windota.models.internal

import windota.models.Lane.Lane
import windota.models.{GameTimeState, PlayerId}

case class UnreactedLaneGank(target: PlayerId, gankers: Seq[PlayerId], gankTime: GameTimeState, deathTime: GameTimeState, lane: Lane)
