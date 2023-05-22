package windota.external.stratz.models

import windota.external.stratz.models.Lane.Lane
import windota.external.stratz.models.Position.Position

case class Match(id: Long, durationSeconds: Long, didRadiantWin: Boolean, players: List[MatchPlayer])

case class MatchPlayer(steamAccountId: Long, heroId: Int, isRadiant: Boolean, kills: Int, deaths: Int, assists: Int, position: Position, lane: Lane)
