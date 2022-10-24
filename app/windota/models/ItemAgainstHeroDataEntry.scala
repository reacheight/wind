package windota.models

import scala.language.implicitConversions

case class ItemAgainstHeroDataEntry(isHeroRadiant: Boolean, radiantHasItem: Boolean, direHasItem: Boolean, radiantNetworth: Int, direNetworth: Int, radiantWon: Boolean, isStomp: Boolean) {
  implicit def b2s(b: Boolean): String = if (b) "1" else "0"
  override def toString = s"${b2s(isHeroRadiant)} ${b2s(!isHeroRadiant)} ${b2s(radiantHasItem)} ${b2s(direHasItem)} $radiantNetworth $direNetworth ${b2s(radiantWon)} ${b2s(isStomp)}"
}
