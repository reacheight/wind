package windota.models.dto

import windota.models.{AbilityId, GameTimeState, HeroId, ItemId, Location, PlayerId}
import windota.models.internal.{DamageAmount, DeathSummaryData}

case class DeathSummary(hero: HeroId, location: Location, time: GameTimeState, respawnTime: Int, damageReceived: Seq[DamageReceived], goldPenalty: Int, goldEarnings: Seq[GoldEarnings])
case class DamageReceived(from: HeroId, attackDamage: DamageAmount, abilityDamage: Seq[AbilityDamage], itemDamage: Seq[ItemDamage])
case class AbilityDamage(abilityId: AbilityId, damage: DamageAmount)
case class ItemDamage(ItemId: ItemId, damage: DamageAmount)
case class GoldEarnings(hero: HeroId, amount: Int)

object DeathSummary {
  def fromInternal(data: DeathSummaryData, heroId: PlayerId => HeroId): DeathSummary = {
    val damageReceived = data.damageReceived.map { case (attackerId, damageDealt) =>
      val abilityDamage = damageDealt.abilityDamage.map { case (abilityId, amount) => AbilityDamage(abilityId, amount) }.toSeq
      val itemDamage = damageDealt.itemDamage.map { case (itemId, amount) => ItemDamage(itemId, amount) }.toSeq
      DamageReceived(heroId(attackerId), damageDealt.attackDamage, abilityDamage, itemDamage)
    }.toSeq
    val goldEarnings = data.goldEarnings.map { case (playerId, amount) => GoldEarnings(heroId(playerId), amount) }.toSeq

    DeathSummary(heroId(data.player), data.location, data.time, data.respawnTime, damageReceived, data.goldPenalty, goldEarnings)
  }
}
