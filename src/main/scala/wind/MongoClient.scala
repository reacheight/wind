package wind

import io.circe.Json
import reactivemongo.api.bson.collection._
import reactivemongo.api.bson.document
import reactivemongo.api.{AsyncDriver, MongoConnection}
import wind.converters._
import wind.models.AnalysisState
import wind.models.AnalysisStatus.AnalysisStatus

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MongoClient {
  val mongoUri = sys.env.getOrElse("MONGO_URI", "idk")
  val driver = AsyncDriver()
  val parsedUri = MongoConnection.fromString(mongoUri)

  val connection = parsedUri.flatMap(uri => driver.connect(uri))
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

  def addState(state: AnalysisState): Future[Unit] = {
    states.flatMap(_.insert.one(state).map(_ => {}))
  }

  def setState(matchId: Long, status: AnalysisStatus): Future[Unit] = {
    val selector = document("matchId" -> matchId)
    val modifier = document("$set" -> document("status" -> status.id))
    states.flatMap(_.update.one(selector, modifier).map(_ => {}))
  }

  def getState(matchId: Long): Future[Option[AnalysisState]] = {
    val query = document("matchId" -> matchId)
    states.flatMap(_.find(query).one[AnalysisState])
  }
}
