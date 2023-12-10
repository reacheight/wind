package windota.models.internal

import windota.models.{GameTimeState, ItemId, PlayerId}

case class UnusedItem(user: PlayerId, target: PlayerId, item: ItemId, time: GameTimeState, withBlink: Boolean)
