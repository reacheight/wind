package windota.external.stratz.models

case class GetMatchItemsResult(matches: List[MatchItems])

case class MatchItems(didRadiantWin: Boolean, players: List[PlayerItems]) {
  val radiant = players.filter(p => p.isRadiant)
  val dire = players.filterNot(p => p.isRadiant)
}

case class PlayerItems(heroId: Int, isRadiant: Boolean, networth: Int, items: List[Int])