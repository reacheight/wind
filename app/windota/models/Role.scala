package windota.models

object Role extends Enumeration {
  type Role = Value
  val SafeLane, MidLane, OffLane, SoftSupport, HardSupport, Undefined = Value
}
