package windota.models.dto

import windota.models.{AbilityId, GameTimeState, HeroId, PlayerId}

case class UnusedAbility(user: HeroId, target: HeroId, ability: AbilityId, time: GameTimeState, withBlink: Boolean)
object UnusedAbility {
  def fromInternal(data: windota.models.internal.UnusedAbility, heroId: PlayerId => HeroId): UnusedAbility =
    UnusedAbility(heroId(data.user), heroId(data.target), data.abilityId, data.time, data.withBlink)
}
