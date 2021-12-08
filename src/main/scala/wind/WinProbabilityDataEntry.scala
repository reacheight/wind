package wind

import wind.Team._

case class WinProbabilityDataEntry(
  time: Int,
  networth: Map[Team, Seq[Int]],
  experience: Map[Team, Seq[Int]],
  towers: Map[Team, Seq[Int]],
  barracks: Map[Team, Seq[Int]],
  isAlive: Map[Team, Seq[Boolean]]) {

    override def toString =
      s"$time ${networth(Radiant).mkString(" ")} ${networth(Dire).mkString(" ")} " +
      s"${experience(Radiant).mkString(" ")} ${experience(Dire).mkString(" ")} " +
      s"${isAlive(Radiant).mkString(" ")} ${isAlive(Dire).mkString(" ")} " +
      s"${towers(Radiant).mkString(" ")} ${towers(Dire).mkString(" ")} " +
      s"${barracks(Radiant).mkString(" ")} ${barracks(Dire).mkString(" ")}"
}
