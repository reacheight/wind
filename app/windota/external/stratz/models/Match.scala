package windota.external.stratz.models

case class Match(id: Long, durationSeconds: Long, didRadiantWin: Boolean, players: List[MatchPlayer])

case class MatchPlayer(steamAccountId: Long, heroId: Int, isRadiant: Boolean, kills: Int, deaths: Int, assists: Int)
