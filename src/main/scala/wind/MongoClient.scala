package wind

import io.circe.Json
import reactivemongo.api.bson.collection._
import reactivemongo.api.bson.document
import reactivemongo.api.{AsyncDriver, MongoConnection}
import wind.converters._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MongoClient {
  val mongoUri = sys.env.getOrElse("MONGO_URI", "idk")
  val driver = AsyncDriver()
  val parsedUri = MongoConnection.fromString(mongoUri)

  val connection = parsedUri.flatMap(uri => driver.connect(uri))
  private def db = connection.flatMap(_.database("analyzes"))
  private def analyzes = db.map(_.collection("analyzes"))

  def saveAnalysis(analysisResult: AnalysisResult): Future[Unit] = {
    analyzes.flatMap(_.insert.one(analysisResult).map(_ =>  {}))
  }

  def getAnalysisJson(matchId: Long): Future[Option[Json]] = {
    val query = document("_id" -> matchId)
    analyzes.flatMap(_.find(query).one[Json])
  }
}
