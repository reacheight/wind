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
            if (downloadReplay(location)) {
              println("Analyzing replay...")
              analyze(replayPath)
              deleteReplayFolder()
            }
        }
      }
      else {
        println("Analyzing replay...")
        analyze(Paths.get(`match`))
      }
    }
    else if (args(0) == "collect") {
      var prevLastMatchId = if (args.length < 3) "" else args(2)
      for (_ <- 1 to args(1).toInt) {
        prevLastMatchId = collect(prevLastMatchId)
      }
    }
  }

  def collect(lastMatchId: String = ""): String = {
    val collector = WinProbabilityDataCollector
    val matches = OdotaClient.getPublicMatches(lastMatchId)
    matches match {
      case Some(matches) =>
        matches.foreach(m => {
          val replayLocation = OdotaClient.getReplayLocation(m.match_id.toString)
          replayLocation match {
            case Some(location) =>
              println(s"Processing match ${m.match_id}")
              if (downloadReplay(location)) {
                println("Collecting data..\n")
                collector.collect(replayPath, m.match_id.toString)
                deleteReplayFolder()
              }
            case _ => println(s"Can't find replay for match ${m.match_id}")
          }
        })

        matches.last.match_id.toString

      case _ =>
        println(s"Can't find matches less than match $lastMatchId")
        ""
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
    result.smokesUsedOnVision.foreach { case (id, times) =>
      println(s"${result.heroName(id)}: ${times.map(_.toString).mkString(", ")}")
    }

    if (result.obsPlacedOnVision.nonEmpty) println("\nObserver wards placed on enemy vision:")
    result.obsPlacedOnVision.foreach { case (id, times) =>
      println(s"${result.heroName(id)}: ${times.map(_.toString).mkString(", ")}")
    }

    if (result.deathsWithBKB.nonEmpty) println("\nBKB not used before death:")
    result.deathsWithBKB.foreach { case (time, id) =>
      println(s"${result.heroName(id)}: ${time.toString}")
    }

    if (result.deathsWithEssenceRing.nonEmpty) println("\nEssence Ring not used before death:")
    result.deathsWithEssenceRing.foreach { case (time, id) =>
      println(s"${result.heroName(id)}: ${time.toString}")
    }

    if (result.deathsWithMekansm.nonEmpty) println("\nMekansm not used before death:")
    result.deathsWithMekansm.foreach { case (time, id) =>
      println(s"${result.heroName(id)}: ${time.toString}")
    }
  }

  def downloadReplay(location: ReplayLocation): Boolean = {
    println(s"Downloading replay..")
    val isDownloaded = ReplayDownloader.downloadReplay(location, compressedReplayPath).nonEmpty

    if (!isDownloaded) {
      println(s"Failed to download replay for match ${location.matchId}")
      false
    }
    else {
      println("Decompressing replay..")
      BZip2Decompressor.decompress(compressedReplayPath, replayPath)
      true
    }
  }

  def deleteReplayFolder(): Unit = {
    compressedReplayPath.toFile.delete()
    replayPath.toFile.delete()
    downloadingDirectory.toFile.delete()
  }
}
