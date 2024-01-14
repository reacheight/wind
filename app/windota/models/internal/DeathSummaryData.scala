package windota.models.internal

import windota.models.{AbilityId, GameTimeState, ItemId, Location, PlayerId}

case class DeathSummaryData(player: PlayerId, location: Location, time: GameTimeState, clTimestamp: Float, damageReceived: Map[PlayerId, DeathSummaryDamageDealt], respawnTime: Int, goldPenalty: Int, goldEarnings: Map[PlayerId, Int])
case class DeathSummaryDamageDealt(attackDamage: DamageAmount, abilityDamage: Map[AbilityId, DamageAmount], itemDamage: Map[ItemId, DamageAmount])
case class DamageAmount(physical: Int, magical: Int, pure: Int)
