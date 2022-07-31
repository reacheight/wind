package windota.models

case class Location(X: Float, Y: Float) {
  override def toString = s"($X, $Y)"
}
