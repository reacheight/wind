package windota.models.internal

import windota.models.PlayerId

case class GoldUpdate(playerId: PlayerId, amount: Int, reason: Int, clTimestamp: Float)
