import { DamageAmount } from "./models/DamageAmount";
import { DamageReceived } from "./models/DamageReceived";

export const calculateFullDamageAmount = (damageAmount: DamageAmount) => damageAmount.pure + damageAmount.magical + damageAmount.physical
export const calculateFullDamageReceived = (damageReceived: DamageReceived) =>
  calculateFullDamageAmount(damageReceived.attackDamage) +
  damageReceived.abilityDamage.reduce((partialSum, b) => partialSum + calculateFullDamageAmount(b.damage), 0) +
  damageReceived.itemDamage.reduce((partialSum, b) => partialSum + calculateFullDamageAmount(b.damage), 0)