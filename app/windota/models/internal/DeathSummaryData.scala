package windota.models.internal

import windota.models.{GameTimeState, PlayerId}

case class DeathSummaryData(player: PlayerId, time: GameTimeState, damageReceived: Map[PlayerId, Int], respawnTime: Int)
