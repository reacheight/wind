package wind

import cats.effect._
import org.http4s.HttpRoutes
import org.http4s.blaze.server._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.middleware._
import wind.converters._

import java.nio.file.{Files, Paths}
import scala.concurrent.ExecutionContext.global

object WindApp extends IOApp {
  private val DownloadingDirectory = "replays"

  val analysisService = HttpRoutes.of[IO] {
    case GET -> Root / "analysis" / matchId =>
      val compressedReplayPath = Paths.get(DownloadingDirectory, s"${matchId}_compressed")
      val replayPath = Paths.get(DownloadingDirectory, s"$matchId.dem")
      if (!Files.exists(replayPath)) {
        val replayLocation = OdotaClient.getReplayLocation(matchId)
        replayLocation
          .flatMap(location => ReplayDownloader.downloadReplay(location, compressedReplayPath))
          .foreach(_ => BZip2Decompressor.decompress(compressedReplayPath, replayPath))
      }

      if (!Files.exists(replayPath))
        NotFound()

      val result = ReplayAnalyzer.analyze(replayPath)
      Ok(result)

  }.orNotFound

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
