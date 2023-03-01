package windota.models

case class BadFight(fight: Fight, seenPlayers: Map[PlayerId, Location])
