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

    println("\nHeroes' roles:")
    result.roles foreach { case (playerId, role) =>
      println(s"${result.heroName(playerId)}: $role")
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
    result.smokesUsedOnVision.foreach { case (time, id) =>
      println(s"$time ${result.heroName(id)}")
    }

    if (result.obsPlacedOnVision.nonEmpty) println("\nObserver wards placed on enemy vision:")
    result.obsPlacedOnVision.foreach { case (time, id) =>
      println(s"$time ${result.heroName(id)}")
    }

    if (result.unusedAbilities.nonEmpty) println("\nAbilities not used before death:")
    result.unusedAbilities.foreach { case (time, id, name) =>
      println(s"${time.toString} ${result.heroName(id)} didn't use $name")
    }

    if (result.unusedOnAllyAbilities.nonEmpty) println("\nAbilities not used on ally:")
    result.unusedOnAllyAbilities foreach { case (time, deadPlayerId, allyId, name) =>
      println(s"${time.toString} ${result.heroName(allyId)} didn't use $name for ${result.heroName(deadPlayerId)}")
    }

    if (result.unusedItems.nonEmpty) println("\nItems not used before death:")
    result.unusedItems.foreach { case (time, id, name) =>
      println(s"${time.toString} ${result.heroName(id)} didn't use $name")
    }

    if (result.ptNotOnStrength.nonEmpty) println("\nPower Treads not on strength:")
    result.ptNotOnStrength.foreach { case (time, id) =>
      println(s"$time ${result.heroName(id)} didn't switch PT to Strength before death")
    }

    if (result.midasEfficiency.nonEmpty) println("\nMidas efficiency:")
    result.midasEfficiency.foreach { case (id, efficiency) =>
      println(s"${result.heroName(id)}: $efficiency")
    }

    println("\nScan usage count:")
    println(s"Radiant: ${result.scanUsageCount(Radiant)}")
    println(s"Dire: ${result.scanUsageCount(Dire)}")

    if (result.wastedCreepwaves.nonEmpty) println("\nWasted creepwaves:")
    result.wastedCreepwaves.foreach { case (time, team, lane, tier) =>
      println(s"$time: $team $lane T$tier")
    }

    if (result.fights.nonEmpty) println("\nFights:")
    result.fights.foreach { case (time, location, heroes) =>
      println(s"$time ${location} (${Util.getLane(location)})\n" +
        s"Participants: ${heroes.map(h => result.heroName(h)).mkString(", ")}\n")
    }

    if (result.badFights.nonEmpty) println(s"\nBad fights: ${result.badFights.mkString(", ")}")
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
