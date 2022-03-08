package wind

import cats.effect._
import org.http4s.HttpRoutes
import org.http4s.blaze.server._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.middleware._
import wind.converters._
import wind.models.{AnalysisState, AnalysisStatus}

import java.nio.file.{Files, Paths}
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

object WindApp extends IOApp {
  private val DownloadingDirectory = "replays"

  val analysisService = HttpRoutes.of[IO] {
    case POST -> Root / "analysis" / matchId =>
      IO.fromFuture(IO(MongoClient.getState(matchId.toLong) flatMap {
        case Some(_) => Future.successful(Created())
        case None =>
          MongoClient.addState(AnalysisState(matchId.toLong, AnalysisStatus.Processing))
            .map(_ => {
              Future {
                val replayLocation = OdotaClient.getReplayLocation(matchId)
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

              Created()
            })
      })).flatten

    case GET -> Root / "analysis" / matchId / "state" =>
      IO.fromFuture(IO(MongoClient.getState(matchId.toLong))) flatMap {
        case Some(state) => Ok(state)
        case None => NotFound()
      }

    case GET -> Root / "analysis" / matchId =>
      IO.fromFuture(IO(MongoClient.getAnalysisJson(matchId.toLong))) flatMap {
        case Some(json) => Ok(json)
        case None => NotFound()
      }

  }.orNotFound

  private def compressedReplayPath(matchId: String) = Paths.get(DownloadingDirectory, s"${matchId}_compressed")
  private def replayPath(matchId: String) = Paths.get(DownloadingDirectory, s"$matchId.dem")

  val corsService = CORS.policy.withAllowOriginAll(analysisService)
  

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .withExecutionContext(global)
      .bindHttp(sys.env.getOrElse("PORT", "8080").toInt, "0.0.0.0")
      .withHttpApp(corsService)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
