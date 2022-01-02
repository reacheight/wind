package wind

import wind.models.Team.{Dire, Radiant}

import java.nio.file.{Path, Paths}

object Main {
  private val downloadingDirectory = Paths.get("tmp")
  private val compressedReplayPath = Paths.get(downloadingDirectory.toString, "replay.dem.bz2")
  private val replayPath = Paths.get(downloadingDirectory.toString, "replay.dem")

  def main(args: Array[String]): Unit = {
    if (args(0) == "analyze") {
      val `match` = args(1)
      if (`match`.forall(_.isDigit)) {
        val replayLocation = OdotaClient.getReplayLocation(`match`)
        replayLocation match {
          case None => println(s"Can't find replay for match ${`match`}.")
          case Some(location) =>
            downloadReplay(location)
            println("Analyzing replay...")
            analyze(replayPath)
            deleteReplayFolder()
        }
      }
      else {
        println("Analyzing replay...")
        analyze(Paths.get(`match`))
      }
    }
    else if (args(0) == "collect") {
      collect()
    }
  }

  def collect(): Unit = {
    val collector = WinProbabilityDataCollector
    val matches = OdotaClient.getPublicMatches
    matches match {
      case Some(matches) => matches.filter(m => m.avg_mmr.getOrElse(0) > 3000).foreach(m => {
        val replayLocation = OdotaClient.getReplayLocation(m.match_id.toString)
        replayLocation match {
          case Some(location) =>
            downloadReplay(location)
            println("Collecting data..")
            collector.collect(replayPath, m.match_id.toString)
            deleteReplayFolder()
        }
      })
    }
  }

  def analyze(replay: Path): Unit = {
    val analyzer = ReplayAnalyzer
    val result = analyzer.analyze(replay)

    println("Couriers location at the start of the game:")
    result.couriers foreach { case (playerId, isOut) =>
      println(s"${result.heroName(playerId)} courier is ${if (isOut) "out of" else "in"} fountain")
    }

    println("\nHeroes' lanes:")
    result.lanes foreach { case (playerId, (firstStageLane, secondStageLane)) =>
      println(s"${result.heroName(playerId)}: $firstStageLane -> $secondStageLane")
    }

    if (result.abilityUsagesWithPT.nonEmpty) println("\nChanging Power Treads' main attribute to Int before ability usage:")
    result.abilityUsagesWithPT foreach { case (playerId, (total, onInt)) =>
      println(s"${result.heroName(playerId)} power treads ability usage: total $total, on Int: $onInt")
    }

    if (result.resourceItemsUsagesWithPT.nonEmpty) println("\nChanging Power Treads' main attribute to Agility before resource refill:")
    result.resourceItemsUsagesWithPT foreach { case (playerId, (total, onAgility)) =>
      println(s"${result.heroName(playerId)} power treads resource refill item usage: total $total, on Agility: ${onAgility}")
    }

    result.laneOutcomes foreach { case (lane, outcome) =>
      println(s"$lane winner: ${outcome.getOrElse("Draw")}")
    }

    if (result.goldFedWithSummons.nonEmpty) println("\nSummon gold fed:")
    result.goldFedWithSummons foreach { case (playerId, gold) =>
      println(s"${result.heroName(playerId)}: $gold")
    }

    println("\nMax smoke stock duration:")
    println(s"Radiant: ${result.maxStockSmokesDuration(Radiant)} sec.")
    println(s"Dire: ${result.maxStockSmokesDuration(Dire)} sec.")

    println("\nMax obs stock duration:")
    println(s"Radiant: ${result.maxStockObsDuration(Radiant)} sec.")
    println(s"Dire: ${result.maxStockObsDuration(Dire)} sec.")

    println("\nGlyph not used on T1 count:")
    println(s"Radiant: ${result.glyphNotUsedOnT1.getOrElse(Radiant, 0)}")
    println(s"Dire: ${result.glyphNotUsedOnT1.getOrElse(Dire, 0)}")

    if (result.smokesUsedOnVision.nonEmpty) println("\nSmoke used on enemy vision:")
    result.smokesUsedOnVision.foreach { case (id, time) =>
      println(s"${result.heroName(id)} ${time.toString}")
    }

    if (result.obsPlacedOnVision.nonEmpty) println("\nObserver wards placed on enemy vision:")
    result.obsPlacedOnVision.foreach { case (id, time) =>
      println(s"${result.heroName(id)} ${time.toString}")
    }
  }

  def downloadReplay(location: ReplayLocation): Unit = {
    println(s"Downloading replay..")
    ReplayDownloader.downloadReplay(location, compressedReplayPath)

    println("Decompressing replay..")
    BZip2Decompressor.decompress(compressedReplayPath, replayPath)
  }

  def deleteReplayFolder(): Unit = {
    compressedReplayPath.toFile.delete()
    replayPath.toFile.delete()
    downloadingDirectory.toFile.delete()
  }
}
