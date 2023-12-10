package windota.models.dto

import windota.models.{GameTimeState, HeroId, ItemId, PlayerId}

case class UnusedItem(user: HeroId, target: HeroId, item: ItemId, time: GameTimeState, withBlink: Boolean)
object UnusedItem {
  def fromInternal(internal: windota.models.internal.UnusedItem, heroId: PlayerId => HeroId) =
    UnusedItem(heroId(internal.user), heroId(internal.target), internal.item, internal.time, internal.withBlink)
}
