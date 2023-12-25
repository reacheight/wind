package windota.models

import windota.models

object DamageType extends Enumeration {
  type DamageType = Value
  val Physical, Magical, Pure = Value

  def fromCLValue(clValue: Int): Option[DamageType] = clValue match {
    case 0 => Some(Physical)
    case 1 => Some(Physical)
    case 2 => Some(Magical)
    case 4 => Some(Pure)
    case _ => None
  }
}
