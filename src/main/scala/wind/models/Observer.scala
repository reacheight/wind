package wind.models

case class Observer(id: Int, location: Location, created: GameTimeState, ended: GameTimeState, owner: PlayerId) {
  def isFullTime = math.abs(ended.gameTime - created.gameTime - 361) < 2
}
