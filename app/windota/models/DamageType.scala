package windota.models

import windota.models

object DamageType extends Enumeration {
  type DamageType = Value
  val Physical, Magical, Pure = Value

  def fromCLValue(clValue: Int): DamageType = clValue match {
    case 1 => Physical
    case 2 => Magical
    case 4 => Pure
  }
}
