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
  def fromPlayerIdKeyMap[T](implicit valueEncoder: Encoder[T]): Encoder[Map[PlayerId, T]] = (map: Map[PlayerId, T]) =>
    map.map { case (id, value) => id.id -> value }.asJson

  def fromTeamKeyMap[T](implicit valueEncoder: Encoder[T]): Encoder[Map[Team, T]] = (map: Map[Team, T]) =>
    map.map { case (team, value) => team.id -> value }.asJson

  implicit val fromPlayerId: Encoder[PlayerId] = (id: PlayerId) => id.id.asJson
  implicit val fromTeam: Encoder[Team] = (team: Team) => team.id.asJson
  implicit val fromLane: Encoder[Lane] = (lane: Lane) => lane.id.asJson

  implicit val fromGameTimeState: Encoder[GameTimeState] = (time: GameTimeState) => time.toString.asJson
  implicit val fromFight: Encoder[Fight] = (fight: Fight) => (fight.outnumberedTeam.get, fight.start).asJson

  implicit val fromPlayerIdStringMap = fromPlayerIdKeyMap[String]
  implicit val fromPlayerIdBooleanMap = fromPlayerIdKeyMap[Boolean]
  implicit val fromPlayerIdIntMap = fromPlayerIdKeyMap[Int]
  implicit val fromPlayerIdFloatMap = fromPlayerIdKeyMap[Float]
  implicit val fromPlayerIdIntTupleMap = fromPlayerIdKeyMap[(Int, Int)]
  implicit val fromTeamIntMap = fromTeamKeyMap[Int]
  implicit val fromTeamTimeMap = fromTeamKeyMap[GameTimeState]

  implicit def analysisEncoder: Encoder[AnalysisResult] = new Encoder[AnalysisResult] {
    override def apply(a: AnalysisResult): Json = Json.obj(
      "heroes" -> a.heroName.asJson,
      "couriers" -> a.couriers.asJson,
      "ability_pt" -> a.abilityUsagesWithPT.asJson,
      "resource_pt" -> a.resourceItemsUsagesWithPT.asJson,
      "ptNotOnStrength" -> a.ptNotOnStrength.asJson,
      "summon_gold" -> a.goldFedWithSummons.asJson,
      "smoke_max_count_time" -> a.maxStockSmokesDuration.asJson,
      "obs_max_count_time" -> a.maxStockObsDuration.asJson,
      "smokes_used_on_vision" -> a.smokesUsedOnVision.asJson,
      "obs_placed_on_vision" -> a.obsPlacedOnVision.asJson,
      "unusedAbilities" -> a.unusedAbilities.asJson,
      "unusedOnAllyAbilities" -> a.unusedOnAllyAbilities.asJson,
      "unusedItems" -> a.unusedItems.asJson,
      "purchases" -> a.purchases.asJson,
      "midasEfficiency" -> a.midasEfficiency.asJson,
      "wastedCreepwaves" -> a.wastedCreepwaves.asJson,
      "badFights" -> a.badFights.asJson
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
