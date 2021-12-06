package wind

import wind.Team._

case class WinProbabilityDataEntry(time: Int, networth: Map[Team, Seq[Int]], experience: Map[Team, Seq[Int]], towers: Map[Team, Seq[Int]]) {
  override def toString = s"$time ${networth(Radiant).mkString(" ")} ${networth(Dire).mkString(" ")} " +
    s"${experience(Radiant).mkString(" ")} ${experience(Dire).mkString(" ")} " +
    s"${towers(Radiant).mkString(" ")} ${towers(Dire).mkString(" ")}"
}
