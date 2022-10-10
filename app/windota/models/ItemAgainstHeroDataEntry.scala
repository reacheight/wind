package windota.models

import scala.language.implicitConversions

case class ItemAgainstHeroDataEntry(isHeroRadiant: Boolean, radiantHasItem: Boolean, direHasItem: Boolean, radiantWon: Boolean) {
  implicit def b2s(b: Boolean): String = if (b) "1" else "0"
  override def toString = s"${b2s(isHeroRadiant)} ${b2s(!isHeroRadiant)} ${b2s(radiantHasItem)} ${b2s(direHasItem)} ${b2s(radiantWon)}"
}
