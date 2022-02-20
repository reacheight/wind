package wind

import cats.effect._
import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.blaze.server._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.middleware._
import wind.models.Lane.Lane
import wind.models.Team.Team
import wind.models.{Fight, GameTimeState, PlayerId}

import java.nio.file.{Files, Paths}
import scala.concurrent.ExecutionContext.global

object WindApp extends IOApp {
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

  implicit def analysisEncoder = new Encoder[AnalysisResult] {
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
