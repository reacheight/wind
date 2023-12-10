package windota.models.internal

import windota.models.{AbilityId, GameTimeState, PlayerId}

case class UnusedAbility(user: PlayerId, target: PlayerId, abilityId: AbilityId, time: GameTimeState, withBlink: Boolean)