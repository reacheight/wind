package wind

import ujson.Obj

import java.nio.file.Paths

object App extends cask.MainRoutes{

  private val downloadingDirectory = Paths.get("tmp")
  private val compressedReplayPath = Paths.get(downloadingDirectory.toString, "replay.dem.bz2")
  private val replayPath = Paths.get(downloadingDirectory.toString, "replay.dem")

  @cask.get("/analysis/:matchId")
  def analyze(matchId: String): Option[Obj] = {
    val replayLocation = OdotaClient.getReplayLocation(matchId)
    replayLocation.map(location => {
      ReplayDownloader.downloadReplay(location, compressedReplayPath)
      BZip2Decompressor.decompress(compressedReplayPath, replayPath)

      val result = ReplayAnalyzer.analyze(replayPath)

      compressedReplayPath.toFile.delete()
      replayPath.toFile.delete()
      downloadingDirectory.toFile.delete()

      ujson.Obj(
        "couriers" -> result.couriers.map { case (id, isOut) => id.toString -> isOut },
        "lanes" -> result.lanes.map { case (id, (firstLane, secondLane)) => id.toString -> Seq(firstLane.id, secondLane.id) },
        "outcome" -> result.laneOutcomes.map { case (lane, team) => lane.id.toString -> (if (team.isEmpty) 2 else team.get.id) },
        "ability_pt" -> result.abilityUsagesWithPT.map { case (id, (total, onInt)) => id.toString -> Seq(total, onInt) },
        "resource_pt" -> result.resourceItemsUsagesWithPT.map { case (id, (total, onAg)) => id.toString -> Seq(total, onAg) },
        "summon_gold" -> result.goldFedWithSummons.map { case (id, gold) => id.toString -> gold },
        "smock_max_count_time" -> result.maxStockSmokesDuration.map { case (team, time) => team.toString -> time },
        "obs_max_count_time" -> result.maxStockObsDuration.map { case (team, time) => team.toString -> time },
        "glyph_not_used" -> result.glyphNotUsedOnT1.map { case (team, count) => team.toString -> count },
        "smokes_used_on_vision" -> result.smokesUsedOnVision.map { case (id, timeState) => id.toString -> timeState.toString },
        "obs_placed_on_vision" -> result.obsPlacedOnVision.map( { case (id, timeState) => id.toString -> timeState.toString })
      )
    })
  }

  initialize()
}