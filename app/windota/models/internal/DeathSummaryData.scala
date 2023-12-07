package windota.models.internal

import windota.models.DamageType.DamageType
import windota.models.{AbilityId, GameTimeState, ItemId, PlayerId}

case class DeathSummaryData(player: PlayerId, time: GameTimeState, clTimestamp: Float, damageReceived: Map[PlayerId, DeathSummaryDamageDealt], respawnTime: Int, goldPenalty: Int, goldEarnings: Map[PlayerId, Int])
case class DeathSummaryDamageDealt(attackDamage: DamageAmount, abilityDamage: Map[AbilityId, DamageAmount], itemDamage: Map[ItemId, DamageAmount])
case class DamageAmount(physical: Int, magical: Int, pure: Int)
