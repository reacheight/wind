package windota

import io.circe.Json
import reactivemongo.api.bson.collection._
import reactivemongo.api.bson.document
import reactivemongo.api.{AsyncDriver, MongoConnection}
import windota.converters._
import windota.models.AnalysisStatus.AnalysisStatus

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MongoClient {
  private val mongoUri = sys.env.getOrElse("MONGO_URI", "idk")
  private val driver = AsyncDriver()
  private val parsedUri = MongoConnection.fromString(mongoUri)

  private val connection = parsedUri.flatMap(uri => driver.connect(uri))
  private def db = connection.flatMap(_.database("analyzes"))
  private def analyzes = db.map(_.collection("analyzes"))
  private def states = db.map(_.collection("states"))

  def saveAnalysis(analysisResult: AnalysisResultInternal): Future[Unit] = {
    analyzes.flatMap(_.insert.one(analysisResult).map(_ =>  {}))
  }

  def getAnalysisJson(matchId: Long): Future[Option[Json]] = {
    val query = document("_id" -> matchId)
    analyzes.flatMap(_.find(query).one[Json])
  }

  def setStatus(matchId: Long, status: AnalysisStatus): Future[Unit] = {
    getStatus(matchId).flatMap {
      case None =>
        val doc = document("_id" -> matchId, "status" -> status)
        states.flatMap(_.insert.one(doc).map(_ => {}))

      case Some(_) =>
        val selector = document("_id" -> matchId)
        val modifier = document("$set" -> document("status" -> status.id))
        states.flatMap(_.update.one(selector, modifier).map(_ => {}))
    }
  }

  def getStatus(matchId: Long): Future[Option[AnalysisStatus]] = {
    val query = document("_id" -> matchId)
    states.flatMap(_.find(query).one[AnalysisStatus])
  }

  def getAnalysisCount: Future[Long] = {
    states.flatMap(_.count())
  }
}
