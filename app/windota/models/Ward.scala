package windota.models

case class Ward(id: Int, sentry: Boolean, location: Location, created: GameTimeState, ended: GameTimeState, owner: PlayerId) {
  val fullDuration = if (sentry) 420 else 360
  def isFullDuration = math.abs(ended.gameTime - created.gameTime - (fullDuration + 1)) < 2
}
