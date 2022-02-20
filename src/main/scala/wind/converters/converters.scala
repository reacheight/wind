package wind

import io.circe.syntax._
import io.circe.{Encoder, Json}
import wind.models.Lane.Lane
import wind.models.Team.Team
import wind.models.{Fight, GameTimeState, PlayerId}

package object converters {
  implicit val playerIdToString = (id: PlayerId) => id.id.toString
  implicit val teamToString = (team: Team) => team.id.toString

  implicit val fromPlayerId: Encoder[PlayerId] = (id: PlayerId) => id.id.asJson
  implicit val fromTeam: Encoder[Team] = (team: Team) => team.id.asJson
  implicit val fromLane: Encoder[Lane] = (lane: Lane) => lane.id.asJson

  implicit val fromGameTimeState: Encoder[GameTimeState] = (time: GameTimeState) => time.toString.asJson
  implicit val fromFight: Encoder[Fight] = (fight: Fight) => (fight.outnumberedTeam.get, fight.start).asJson

  implicit class MapExtensions[K, V](val map: Map[K, V]) {
    def toStringKeyMap(implicit keyToString: K => String) = map.map { case (k, v) => keyToString(k) -> v }
  }

  implicit object AnalysisResultEncoder extends Encoder[AnalysisResult] {
    override def apply(a: AnalysisResult): Json = Json.obj(
      "info" -> Json.obj(
        "heroes" -> a.heroName.toStringKeyMap.asJson
      ),
      "analysis" -> Json.obj(
        "couriers" -> a.couriers.toStringKeyMap.asJson,
        "abilityPt" -> a.abilityUsagesWithPT.toStringKeyMap.asJson,
        "resourcePt" -> a.resourceItemsUsagesWithPT.toStringKeyMap.asJson,
        "ptNotOnStrength" -> a.ptNotOnStrength.asJson,
        "summonGold" -> a.goldFedWithSummons.toStringKeyMap.asJson,
        "smokeMaxCountTime" -> a.maxStockSmokesDuration.toStringKeyMap.asJson,
        "obsMaxCountTime" -> a.maxStockObsDuration.toStringKeyMap.asJson,
        "smokesUsedOnVision" -> a.smokesUsedOnVision.asJson,
        "obsPlacedOnVision" -> a.obsPlacedOnVision.asJson,
        "unusedAbilities" -> a.unusedAbilities.asJson,
        "unusedOnAllyAbilities" -> a.unusedOnAllyAbilities.asJson,
        "unusedItems" -> a.unusedItems.asJson,
        "purchases" -> a.purchases.asJson,
        "midasEfficiency" -> a.midasEfficiency.toStringKeyMap.asJson,
        "wastedCreepwaves" -> a.wastedCreepwaves.asJson,
        "badFights" -> a.badFights.asJson
      )
    )
  }
}
