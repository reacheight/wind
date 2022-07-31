package windota

import io.circe.bson._
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.{Encoder, Json}
import reactivemongo.api.bson._
import windota.models.AnalysisStatus._
import windota.models.Lane.Lane
import windota.models.Team.Team
import windota.models.{AnalysisStatus, Fight, GameTimeState, HeroId, PlayerId}

import scala.util.{Failure, Success}

package object converters {
  // Game types
  implicit val playerIdToString = (id: PlayerId) => id.id.toString
  implicit val teamToString = (team: Team) => team.id.toString

  implicit val fromPlayerId: Encoder[PlayerId] = (id: PlayerId) => id.id.asJson
  implicit val fromHeroId: Encoder[HeroId] = (id: HeroId) => id.id.asJson
  implicit val fromTeam: Encoder[Team] = (team: Team) => team.id.asJson
  implicit val fromLane: Encoder[Lane] = (lane: Lane) => lane.id.asJson

  implicit val fromGameTimeState: Encoder[GameTimeState] = (time: GameTimeState) => time.toString.asJson
  implicit val fromFight: Encoder[Fight] = (fight: Fight) => (fight.outnumberedTeam.get, fight.start).asJson

  implicit class MapExtensions[K, V](val map: Map[K, V]) extends AnyVal {
    def toStringKeyMap(implicit keyToString: K => String) = map.map { case (k, v) => keyToString(k) -> v }
  }

  // Analysis result
  implicit def analysisResultEncoder: Encoder[AnalysisResultInternal] = (analysis: AnalysisResultInternal) =>
    AnalysisResultMapper.toJsonModel(analysis).asJson

  implicit def analysisResultWriter: BSONDocumentWriter[AnalysisResultInternal] = (analysis: AnalysisResultInternal) =>
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

  // Analysis status
  implicit def analysisStatusEncoder: Encoder[AnalysisStatus] = (status: AnalysisStatus) => status.id.asJson

  implicit def analysisStatusReader: BSONDocumentReader[AnalysisStatus] = (doc: BSONDocument) =>
    doc.get("status") match {
      case None => Failure(new Exception)
      case Some(bson) => bson match {
        case BSONInteger(i) => Success(AnalysisStatus(i))
        case _ => Failure(new Exception)
      }
    }

  implicit def analysisStatusWriter: BSONWriter[AnalysisStatus] = (status: AnalysisStatus) => Success(BSONInteger(status.id))
}
