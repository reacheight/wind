package wind

import io.circe.bson._
import io.circe.syntax._
import io.circe.{Encoder, Json}
import reactivemongo.api.bson._
import wind.models.AnalysisStatus._
import wind.models.Lane.Lane
import wind.models.Team.Team
import wind.models.{AnalysisState, AnalysisStatus, Fight, GameTimeState, PlayerId}

import scala.util.{Failure, Success}

package object converters {
  implicit val playerIdToString = (id: PlayerId) => id.id.toString
  implicit val teamToString = (team: Team) => team.id.toString

  implicit val fromPlayerId: Encoder[PlayerId] = (id: PlayerId) => id.id.asJson
  implicit val fromTeam: Encoder[Team] = (team: Team) => team.id.asJson
  implicit val fromLane: Encoder[Lane] = (lane: Lane) => lane.id.asJson

  implicit val fromGameTimeState: Encoder[GameTimeState] = (time: GameTimeState) => time.toString.asJson
  implicit val fromFight: Encoder[Fight] = (fight: Fight) => (fight.outnumberedTeam.get, fight.start).asJson

  implicit class MapExtensions[K, V](val map: Map[K, V]) extends AnyVal {
    def toStringKeyMap(implicit keyToString: K => String) = map.map { case (k, v) => keyToString(k) -> v }
  }

  implicit def analysisResultEncoder: Encoder[AnalysisResult] = (analysis: AnalysisResult) => Json.obj(
    "info" -> Json.obj(
      "heroes" -> analysis.heroName.toStringKeyMap.asJson
    ),
    "analysis" -> Json.obj(
      "couriers" -> analysis.couriers.toStringKeyMap.asJson,
      "abilityPt" -> analysis.abilityUsagesWithPT.toStringKeyMap.asJson,
      "resourcePt" -> analysis.resourceItemsUsagesWithPT.toStringKeyMap.asJson,
      "ptNotOnStrength" -> analysis.ptNotOnStrength.asJson,
      "summonGold" -> analysis.goldFedWithSummons.toStringKeyMap.asJson,
      "smokeMaxCountTime" -> analysis.maxStockSmokesDuration.toStringKeyMap.asJson,
      "obsMaxCountTime" -> analysis.maxStockObsDuration.toStringKeyMap.asJson,
      "smokesUsedOnVision" -> analysis.smokesUsedOnVision.asJson,
      "obsPlacedOnVision" -> analysis.obsPlacedOnVision.asJson,
      "unusedAbilities" -> analysis.unusedAbilities.asJson,
      "unusedOnAllyAbilities" -> analysis.unusedOnAllyAbilities.asJson,
      "unusedItems" -> analysis.unusedItems.asJson,
      "purchases" -> analysis.purchases.asJson,
      "midasEfficiency" -> analysis.midasEfficiency.toStringKeyMap.asJson,
      "wastedCreepwaves" -> analysis.wastedCreepwaves.asJson,
      "notTankedCreepwaves" -> analysis.notTankedCreepwaves.asJson,
      "badFights" -> analysis.badFights.asJson,
      "glyphUsedOnDeadT2" -> analysis.glyphOnDeadT2.toStringKeyMap.asJson,
      "badSmokeFights" -> analysis.smokeOnVisionButWonFight.asJson,
    )
  )

  implicit def analysisStateJsonEncoder: Encoder[AnalysisState] = (state: AnalysisState) => Json.obj(
    "matchId" -> state.matchId.asJson,
    "status" -> state.status.id.asJson
  )

  implicit def analysisResultWriter: BSONDocumentWriter[AnalysisResult] = (analysis: AnalysisResult) =>
    jsonToBson(analysis.asJson) match {
      case Left(err) => Failure(err)
      case Right(bson) => Success(BSONDocument("_id" -> analysis.matchId, "result" -> bson))
    }

  implicit def analysisResultJsonReader: BSONDocumentReader[Json] = (doc: BSONDocument) =>
    doc.get("result") match {
      case None => Failure(new Exception)
      case Some(bson) => bsonToJson(bson) match {
        case Left(err) => Failure(err)
        case Right(json) => Success(json)
      }
    }

  implicit def analysisStatusReader: BSONReader[AnalysisStatus] = {
    case BSONInteger(i) => Success(AnalysisStatus(i))
    case _ => Failure(new Exception)
  }

  implicit def analysisStatusWriter: BSONWriter[AnalysisStatus] = (status: AnalysisStatus) => Success(BSONInteger(status.id))

  implicit val analysisStateHandler: BSONDocumentHandler[AnalysisState] =
    Macros.handler[AnalysisState]
}
