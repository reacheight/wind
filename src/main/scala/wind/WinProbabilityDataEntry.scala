package wind

import wind.Team._

case class WinProbabilityDataEntry(
  time: Int,
  networth: Map[Team, Seq[Int]],
  experience: Map[Team, Seq[Int]],
  towers: Map[Team, Seq[Int]],
  barracks: Map[Team, Seq[Int]],
  respawnTime: Map[Team, Seq[Float]],
  buybackState: Map[Team, Seq[Boolean]]) {

    override def toString =
      s"$time ${networth(Radiant).mkString(" ")} ${networth(Dire).mkString(" ")} " +
      s"${experience(Radiant).mkString(" ")} ${experience(Dire).mkString(" ")} " +
      s"${respawnTime(Radiant).mkString(" ")} ${respawnTime(Dire).mkString(" ")} " +
      s"${towers(Radiant).mkString(" ")} ${towers(Dire).mkString(" ")} " +
      s"${barracks(Radiant).mkString(" ")} ${barracks(Dire).mkString(" ")} " +
      s"${buybackState(Radiant).mkString(" ")} ${buybackState(Dire).mkString(" ")}"
}
