package wind

import cask.Response
import ujson.Obj

import java.nio.file.{Files, Paths}

object App extends cask.MainRoutes{

  private val DownloadingDirectory = "replays"

  @cask.get("/analysis/:matchId")
  def analyze(matchId: String): Response[Obj] = {
    val compressedReplayPath = Paths.get(DownloadingDirectory, s"${matchId}_compressed")
    val replayPath = Paths.get(DownloadingDirectory, matchId)
    if (!Files.exists(replayPath)) {
      val replayLocation = OdotaClient.getReplayLocation(matchId)
      replayLocation
        .flatMap(location => ReplayDownloader.downloadReplay(location, compressedReplayPath))
        .foreach(_ => BZip2Decompressor.decompress(compressedReplayPath, replayPath))
    }

    if (!Files.exists(replayPath))
      return cask.Response(ujson.Obj(), 400, headers = Seq(("Access-Control-Allow-Origin", "*")))

    val result = ReplayAnalyzer.analyze(replayPath)

    //todo add task to delete replay files after some time

    val responseData = ujson.Obj(
      "couriers" -> result.couriers.map { case (id, isOut) => id.toString -> isOut },
      "lanes" -> result.lanes.map { case (id, (firstLane, secondLane)) => id.toString -> Seq(firstLane.id, secondLane.id) },
      "roles" -> result.roles.map { case (id, role) => id.toString -> role.id },
      "outcome" -> result.laneOutcomes.map { case (lane, team) => lane.id.toString -> (if (team.isEmpty) 2 else team.get.id) },
      "ability_pt" -> result.abilityUsagesWithPT.map { case (id, (total, onInt)) => id.toString -> Seq(total, onInt) },
      "resource_pt" -> result.resourceItemsUsagesWithPT.map { case (id, (total, onAg)) => id.toString -> Seq(total, onAg) },
      "ptNotOnStrength" -> result.ptNotOnStrength.map { case (time, id) => Seq(time.toString, id.toString) },
      "summon_gold" -> result.goldFedWithSummons.map { case (id, gold) => id.toString -> gold },
      "smoke_max_count_time" -> result.maxStockSmokesDuration.map { case (team, time) => team.toString -> time },
      "obs_max_count_time" -> result.maxStockObsDuration.map { case (team, time) => team.toString -> time },
      "glyph_not_used" -> result.glyphNotUsedOnT1.map { case (team, count) => team.toString -> count },
      "smokes_used_on_vision" -> result.smokesUsedOnVision.map { case (time, id) => Seq(time.toString, id.toString) },
      "obs_placed_on_vision" -> result.obsPlacedOnVision.map { case (time, id) => Seq(time.toString, id.toString) },
      "heroes" -> result.heroName.map { case(id, name) => id.toString -> name },
      "unusedAbilities" -> result.unusedAbilities.map { case (time, id, name) => Seq(time.toString, id.toString, name) },
      "unusedItems" -> result.unusedItems.map { case (time, id, name) => Seq(time.toString, id.toString, name) },
    )

    cask.Response(responseData, headers = Seq(("Access-Control-Allow-Origin", "*")))
  }

  initialize()
}