package wind

import wind.models.ReplayLocation
import wind.models.Team.{Dire, Radiant}

import java.nio.file.{Files, Path, Paths}

object Main {
  private val CacheReplayDirectory = Paths.get("replays")
  private val TmpReplayDirectory = Paths.get("tmp")

  def cacheReplayPath(matchId: String) = Paths.get(CacheReplayDirectory.toString, s"$matchId.dem")
  def tmpReplayPath(matchId: String) = Paths.get(TmpReplayDirectory.toString, s"$matchId.dem")
  def compressedReplayPath(matchId: String) = Paths.get(TmpReplayDirectory.toString, s"${matchId}_compressed")

  def main(args: Array[String]): Unit = {
    if (args(0) == "analyze") {
      val `match` = args(1)
      if (`match`.forall(_.isDigit)) {
        if (!Files.exists(cacheReplayPath(`match`))) {
          val replayLocation = OdotaClient.getReplayLocation(`match`)
          replayLocation match {
            case None => println(s"Can't find replay for match ${`match`}.")
            case Some(location) =>
              if (downloadReplay(location, compressedReplayPath(`match`), cacheReplayPath(`match`)))
                analyze(cacheReplayPath(`match`))
          }
        }
        else
          analyze(cacheReplayPath(`match`))
      }
      else {
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
              if (downloadReplay(location, compressedReplayPath(m.match_id.toString), tmpReplayPath(m.match_id.toString))) {
                println("Collecting data..\n")
                collector.collect(tmpReplayPath(m.match_id.toString), m.match_id.toString)
                deleteTmpFolder()
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
    println("Analyzing..")

    val analyzer = ReplayAnalyzer
    val result = analyzer.analyze(replay)

    println("Couriers location at the start of the game:")
    result.couriers foreach { case (playerId, (isOut, isVersusMK)) =>
      println(s"${result.heroName(playerId)} courier is ${if (isOut) "out of" else "in"} fountain ${if (isOut && isVersusMK) "versus Monkey King \uD83E\uDD21" else ""}")
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

    if (result.glyphOnDeadT2.flatMap(_._2).nonEmpty) println("\nGlyph used on dead T2:")
    result.glyphOnDeadT2.foreach { case (team, usages) =>
      if (usages.nonEmpty) println(s"$team: ${usages.mkString(", ")}")
    }

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

    if (result.unusedOnAllyWithBlinkAbilities.nonEmpty) println("\nAbilities not used with Blink Dagger on ally:")
    result.unusedOnAllyWithBlinkAbilities foreach { case (time, deadPlayerId, allyId, name) =>
      println(s"${time.toString} ${result.heroName(allyId)} didn't use Blink Dagger + $name for ${result.heroName(deadPlayerId)}")
    }

    if (result.unusedItems.nonEmpty) println("\nItems not used before death:")
    result.unusedItems.foreach { case (time, id, name) =>
      println(s"${time.toString} ${result.heroName(id)} didn't use $name")
    }

    if (result.unusedOnAllyItems.nonEmpty) println("\nItems not used on ally:")
    result.unusedOnAllyItems foreach { case (time, deadPlayerId, allyId, item) =>
      println(s"${time.toString} ${result.heroName(allyId)} didn't use $item for ${result.heroName(deadPlayerId)}")
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

    if (result.notTankedCreepwaves.nonEmpty) println("\nNot tanked creepwaves:")
    result.notTankedCreepwaves.foreach { case (time, team, lane, players) =>
      println(s"${players.map(id => result.heroName(id)).mkString(", ")} didn't tank creepwave at $time in $lane lane")
    }

    if (result.fights.nonEmpty) println("\nFights:")
    result.fights.foreach(fight => {
      println(s"${fight.start} - ${fight.end} ${fight.location} (${Util.getLane(fight.location)})\n" +
        s"Participants (${fight.radiantParticipants.size} radiant vs ${fight.direParticipants.size} dire): " +
        s"${fight.participants.map(h => result.heroName(h)).mkString(", ")}\n" +
        s"Dead heroes: ${fight.dead.map(h => result.heroName(h)).mkString(", ")}\n"
      )
    })

    if (result.badFights.nonEmpty) println("\nBad fights:")
    result.badFights.foreach(badFight =>
      println(s"${badFight.fight.start}, seen heroes: ${badFight.seenPlayers.map(id => result.heroName(id)).mkString(", ")}")
    )

    if (result.smokeFights.nonEmpty) println(s"\nSmoke fights:")
    result.smokeFights.foreach { case (smokeTimes, fight) =>
      println(s"${fight.start}: ${smokeTimes.map { case (team, smokeTime) => s"$team smoked at $smokeTime" }.mkString(", ")}")
    }

    result.smokeOnVisionButWonFight.foreach { case (fightTime, smokeTime, smokeTeam) =>
      println(s"$smokeTeam used smoke on enemy vision at $smokeTime, but ${Util.getOppositeTeam(smokeTeam)} didn't react and lost fight anyway at $fightTime.")
    }

    if (result.overlappedStuns.nonEmpty) println("\nOverlapped stuns:")
    result.overlappedStuns.foreach { case (time, stunnedId, attackerId) =>
      println(s"${result.heroName(attackerId)} stunned ${result.heroName(stunnedId)} too early at $time")
    }
  }

  def downloadReplay(location: ReplayLocation, compressedPath: Path, replayPath: Path): Boolean = {
    println(s"Downloading replay for match ${location.matchId} to $replayPath..")
    val isDownloaded = ReplayDownloader.downloadReplay(location, compressedPath).nonEmpty

    if (!isDownloaded) {
      println(s"Failed to download replay for match ${location.matchId}")
      false
    }
    else {
      println("Decompressing replay..")
      BZip2Decompressor.decompress(compressedPath, replayPath)
      compressedPath.toFile.delete()
      true
    }
  }

  def deleteTmpFolder(): Unit = {
    TmpReplayDirectory.toFile.listFiles().foreach(_.delete())
    TmpReplayDirectory.toFile.delete()
  }
}
