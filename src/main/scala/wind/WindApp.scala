package wind

import cats.effect._
import io.circe.{Encoder, Json}
import org.http4s.HttpRoutes
import org.http4s.blaze.server._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.middleware._
import wind.models.Lane.Lane
import wind.models.Team.Team
import wind.models.{GameTimeState, PlayerId}

import java.nio.file.{Files, Paths}
import scala.concurrent.ExecutionContext.global

object WindApp extends IOApp {
  implicit def fromStringSeq: Encoder[Seq[String]] = (a: Seq[String]) =>
    Json.fromValues(a.map(Json.fromString))

  implicit def stringMapEncoder: Encoder[Map[PlayerId, String]] = (a: Map[PlayerId, String]) =>
    Json.fromFields(a.map { case (id, value) => id.toString -> Json.fromString(value) })

  implicit def boolMapEncoder: Encoder[Map[PlayerId, Boolean]] = (a: Map[PlayerId, Boolean]) =>
    Json.fromFields(a.map { case (id, value) => id.toString -> Json.fromBoolean(value) })

  implicit def intMapEncoder: Encoder[Map[PlayerId, Int]] = (a: Map[PlayerId, Int]) =>
    Json.fromFields(a.map { case (id, value) => id.toString -> Json.fromInt(value)})

  implicit def floatMapEncoder: Encoder[Map[PlayerId, Float]] = (a: Map[PlayerId, Float]) =>
    Json.fromFields(a.map { case (id, value) => id.toString -> Json.fromFloatOrNull(value)})

  implicit def intTupleEncoder: Encoder[(Int, Int)] = (a: (Int, Int)) =>
    Json.fromValues(Seq(Json.fromInt(a._1), Json.fromInt(a._2)))

  implicit def tupleIntMapEncoder: Encoder[Map[PlayerId, (Int, Int)]] = (a: Map[PlayerId, (Int, Int)]) =>
    Json.fromFields(a.map { case (id, tuple) => id.toString -> intTupleEncoder(tuple) })

  implicit def timeToPlayerId: Encoder[Seq[(GameTimeState, PlayerId)]] = (a: Seq[(GameTimeState, PlayerId)]) =>
    Json.fromFields(a.map { case (time, id) => time.toString -> Json.fromString(id.toString) })

  implicit def teamToTime: Encoder[Map[Team, GameTimeState]] = (a: Map[Team, GameTimeState]) =>
    Json.fromFields(a.map { case (team, time) => team.id.toString -> Json.fromString(time.toString) })

  implicit def teamToInt: Encoder[Map[Team, Int]] = (a: Map[Team, Int]) =>
    Json.fromFields(a.map { case (team, value) => team.id.toString -> Json.fromInt(value) })

  implicit def timeIdStringSeq: Encoder[Seq[(GameTimeState, PlayerId, String)]] = (a: Seq[(GameTimeState, PlayerId, String)]) =>
    Json.fromValues(a.map { case (time, id, value) => Json.fromValues(Seq(Json.fromString(time.toString), Json.fromString(id.toString), Json.fromString(value))) })

  implicit def timeIdIdStringSeq: Encoder[Seq[(GameTimeState, PlayerId, PlayerId, String)]] = (a: Seq[(GameTimeState, PlayerId, PlayerId, String)]) =>
    Json.fromValues(a.map { case (time, id1, id2, value) => Json.fromValues(Seq(Json.fromString(time.toString), Json.fromString(id1.toString), Json.fromString(id2.toString), Json.fromString(value))) })

  implicit def fromPurchases: Encoder[Map[String, Seq[(String, Int)]]] = (a: Map[String, Seq[(String, Int)]]) =>
    Json.fromFields(a.map { case (hero, list) => hero -> Json.fromValues(list.map { case (item, time) => Json.fromValues(Seq(Json.fromString(item), Json.fromInt(time))) })})

  implicit def fromWastedCreepwaves: Encoder[Seq[(GameTimeState, Team, Lane, Int)]] = (a: Seq[(GameTimeState, Team, Lane, Int)]) =>
    Json.fromValues(a.map { case (time, team, lane, tier) => fromStringSeq(Seq(time.toString, team.id.toString, lane.id.toString, tier.toString)) })

  implicit def fromTimePlayerIdSeq: Encoder[Seq[(GameTimeState, PlayerId)]] = (a: Seq[(GameTimeState, PlayerId)]) =>
    Json.fromValues(a.map { case (time, id) => Json.fromValues(Seq(Json.fromString(time.toString), Json.fromInt(id.id))) })

  implicit def analysisEncoder: Encoder[AnalysisResult] = new Encoder[AnalysisResult] {
    override def apply(a: AnalysisResult): Json = Json.obj(
      "heroes" -> stringMapEncoder(a.heroName),
      "couriers" -> boolMapEncoder(a.couriers),
      "ability_pt" -> tupleIntMapEncoder(a.abilityUsagesWithPT),
      "resource_pt" -> tupleIntMapEncoder(a.resourceItemsUsagesWithPT),
      "ptNotOnStrength" -> fromTimePlayerIdSeq(a.ptNotOnStrength),
      "summon_gold" -> intMapEncoder(a.goldFedWithSummons),
      "smoke_max_count_time" -> teamToInt(a.maxStockSmokesDuration),
      "obs_max_count_time" -> teamToInt(a.maxStockObsDuration),
      "smokes_used_on_vision" -> fromTimePlayerIdSeq(a.smokesUsedOnVision),
      "obs_placed_on_vision" -> fromTimePlayerIdSeq(a.obsPlacedOnVision),
      "unusedAbilities" -> timeIdStringSeq(a.unusedAbilities),
      "unusedOnAllyAbilities" -> timeIdIdStringSeq(a.unusedOnAllyAbilities),
      "unusedItems" -> timeIdStringSeq(a.unusedItems),
      "purchases" -> fromPurchases(a.purchases),
      "midasEfficiency" -> floatMapEncoder(a.midasEfficiency),
      "wastedCreepwaves" -> fromWastedCreepwaves(a.wastedCreepwaves),
      "badFights" -> Json.fromValues(a.badFights.map(fight => fromStringSeq(Seq(fight.outnumberedTeam.get.id.toString, fight.start.toString))))
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

  val corsService = CORS(analysisService)
  

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](global)
      .bindHttp(sys.env.getOrElse("PORT", "8080").toInt, "localhost")
      .withHttpApp(corsService)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
