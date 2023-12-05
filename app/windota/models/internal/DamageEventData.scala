package windota.models.internal

import windota.models.DamageType.DamageType
import windota.models.{AbilityId, GameTimeState, ItemId, PlayerId}

case class DamageEventData(target: PlayerId, attacker: PlayerId, amount: Int, time: GameTimeState, ability: Option[AbilityId], item: Option[ItemId], damageType: DamageType) {
  val isAttack: Boolean = ability.isEmpty && item.isEmpty
}
