package windota.models

case class GameTimeState(preGameStarted: Boolean, gameStarted: Boolean, gameTime: Float, transitionTime: Float) {
  def addSeconds(seconds: Float): GameTimeState = {
    val newSeconds = gameTime + seconds
    val newGameStarted = newSeconds >= 0
    GameTimeState(true, newGameStarted, newSeconds, transitionTime)
  }

  override def toString: String = {
    val minutes = gameTime.toInt.abs / 60
    val seconds = gameTime.toInt.abs % 60
    val secondsStr = if (seconds < 10) s"0$seconds" else seconds
    val sign = if (gameStarted) "" else "-"
    if (preGameStarted) s"$sign$minutes:$secondsStr" else "not started"
  }
}
