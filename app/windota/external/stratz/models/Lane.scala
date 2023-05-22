package windota.external.stratz.models

object Lane extends Enumeration {
  type Lane = Value
  val SafeLane, MidLane, OffLane, Jungle, Roaming, Unknown = Value
}
