package wind.models

case class GameTimeState(preGameStarted: Boolean, gameStarted: Boolean, gameTime: Float) {
  override def toString: String = {
    val minutes = gameTime.toInt.sign * gameTime.toInt.abs / 60
    val seconds = gameTime.toInt.abs % 60
    val secondsStr = if (seconds < 10) s"0$seconds" else seconds
    if (preGameStarted) s"$minutes:$secondsStr" else "not started"
  }
}
