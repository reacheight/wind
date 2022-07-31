package windota.models

case class Stun(start: GameTimeState, duration: Float) {
  val end = start.addSeconds(duration)
}
