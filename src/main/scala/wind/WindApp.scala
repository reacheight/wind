package wind

import cats.effect._
import org.http4s.blaze.server._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.middleware._
import org.http4s.{HttpRoutes, StaticFile}
import wind.constants.Heroes
import wind.converters._
import wind.models.{AnalysisState, AnalysisStatus}

import java.nio.file.{Files, Paths}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

object WindApp extends IOApp {
  private val DownloadingDirectory = "replays"
  private def compressedReplayPath(matchId: String) = Paths.get(DownloadingDirectory, s"${matchId}_compressed")
  private def replayPath(matchId: String) = Paths.get(DownloadingDirectory, s"$matchId.dem")

  private def startAnalysis(matchId: String): Unit = {
    try {
      val stratzReplayLocation = StratzClient.getReplayLocation(matchId)
    } catch {
      case e => println(e.getMessage)
    }

    val replayLocation = StratzClient.getReplayLocation(matchId) match {
      case None => OdotaClient.getReplayLocation(matchId)
      case location => location
    }

    replayLocation
      .flatMap(location => ReplayDownloader.downloadReplay(location, compressedReplayPath(matchId)))
      .foreach(_ => BZip2Decompressor.decompress(compressedReplayPath(matchId), replayPath(matchId)))

    if (!Files.exists(replayPath(matchId))) {
      MongoClient.setState(matchId.toLong, AnalysisStatus.Failed)
    } else {
      val analysis = ReplayAnalyzer.analyze(replayPath(matchId))
      Future { Paths.get(DownloadingDirectory).toFile.listFiles().foreach(_.delete()) }
      MongoClient.saveAnalysis(analysis)
      MongoClient.setState(matchId.toLong, AnalysisStatus.Finished)
    }
  }

  val analysisService = HttpRoutes.of[IO] {
    case POST -> Root / "analysis" / matchId =>
      IO.fromFuture(IO(MongoClient.getState(matchId.toLong) flatMap {

        case Some(state) if state.status == AnalysisStatus.Failed =>
          MongoClient.setState(matchId.toLong, AnalysisStatus.Processing)
            .map(_ => {
              Future { startAnalysis(matchId) }
              Created()
            })

        case None =>
          MongoClient.addState(AnalysisState(matchId.toLong, AnalysisStatus.Processing))
            .map(_ => {
              Future { startAnalysis(matchId) }
              Created()
            })

        case _ => Future.successful(Created())
      })).flatten

    case GET -> Root / "analysis" / LongVar(matchId) / "state" =>
      IO.fromFuture(IO(MongoClient.getState(matchId))) flatMap {
        case Some(state) => Ok(state)
        case None => NotFound()
      }

    case GET -> Root / "analysis" / LongVar(matchId) =>
      IO.fromFuture(IO(MongoClient.getAnalysisJson(matchId))) flatMap {
        case Some(json) => Ok(json)
        case None => NotFound()
      }

    case request @ GET -> Root / "icon" / IntVar(heroId) =>
      val heroTag = Heroes.getTag(heroId)
      StaticFile.fromResource(s"icons/$heroTag.png", Some(request)).getOrElseF(NotFound())

  }.orNotFound

  val corsService = CORS.policy.withAllowOriginAll(analysisService)
  

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .withExecutionContext(global)
      .bindHttp(sys.env.getOrElse("PORT", "8000").toInt, "0.0.0.0")
      .withHttpApp(corsService)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
