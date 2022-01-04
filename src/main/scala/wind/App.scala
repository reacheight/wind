package wind

import cask.Response
import ujson.Obj

import java.nio.file.Paths

object App extends cask.MainRoutes{

  private val DownloadingDirectory = "replays"

  @cask.get("/analysis/:matchId")
  def analyze(matchId: String): Response[Obj] = {
    val replayLocation = OdotaClient.getReplayLocation(matchId)
    val compressedReplayPath = Paths.get(DownloadingDirectory, s"${matchId}_compressed")
    val replayPath = Paths.get(DownloadingDirectory, matchId)
    replayLocation.flatMap(location => {
      ReplayDownloader.downloadReplay(location, compressedReplayPath).map(_ => {
        BZip2Decompressor.decompress(compressedReplayPath, replayPath)

        val result = ReplayAnalyzer.analyze(replayPath)

        compressedReplayPath.toFile.delete()
        replayPath.toFile.delete()

        val responseData = ujson.Obj(
          "couriers" -> result.couriers.map { case (id, isOut) => id.toString -> isOut },
          "lanes" -> result.lanes.map { case (id, (firstLane, secondLane)) => id.toString -> Seq(firstLane.id, secondLane.id) },
          "outcome" -> result.laneOutcomes.map { case (lane, team) => lane.id.toString -> (if (team.isEmpty) 2 else team.get.id) },
          "ability_pt" -> result.abilityUsagesWithPT.map { case (id, (total, onInt)) => id.toString -> Seq(total, onInt) },
          "resource_pt" -> result.resourceItemsUsagesWithPT.map { case (id, (total, onAg)) => id.toString -> Seq(total, onAg) },
          "summon_gold" -> result.goldFedWithSummons.map { case (id, gold) => id.toString -> gold },
          "smock_max_count_time" -> result.maxStockSmokesDuration.map { case (team, time) => team.toString -> time },
          "obs_max_count_time" -> result.maxStockObsDuration.map { case (team, time) => team.toString -> time },
          "glyph_not_used" -> result.glyphNotUsedOnT1.map { case (team, count) => team.toString -> count },
          "smokes_used_on_vision" -> result.smokesUsedOnVision.map { case (id, times) => id.toString -> times.map(_.toString) },
          "obs_placed_on_vision" -> result.obsPlacedOnVision.map{ case (id, times) => id.toString -> times.map(_.toString) },
          "heroes" -> result.heroName.map { case(id, name) => id.toString -> name }
        )

        cask.Response(responseData, headers = Seq(("Access-Control-Allow-Origin", "http://127.0.0.1:5500")))
      })
    }) match {
      case Some(response) => response
      case _ => cask.Response(ujson.Obj(), 400, headers = Seq(("Access-Control-Allow-Origin", "http://127.0.0.1:5500")))
    }
  }

  initialize()
}