package windota

import io.circe.syntax.EncoderOps
import skadistats.clarity.processor.runner.SimpleRunner
import skadistats.clarity.source.MappedFileSource
import windota.constants.{Abilities, Heroes, Items}
import windota.models.ReplayLocation
import windota.models.Team._
import windota.models.Lane._
import windota.processors.CurrentMapProcessor
import windota.converters._
import windota.external.stratz.StratzClient
import windota.external.valve.ValveClient

import java.nio.file.{Files, Path, Paths}
import scala.util.{Failure, Success, Using}

object Main {
  private val CacheReplayDirectory = Paths.get("replays")
  private val TmpReplayDirectory = Paths.get("tmp")

  def cacheReplayPath(matchId: String) = Paths.get(CacheReplayDirectory.toString, s"$matchId.dem")
  def tmpReplayPath(matchId: String) = Paths.get(TmpReplayDirectory.toString, s"$matchId.dem")
  def compressedReplayPath(matchId: String) = Paths.get(TmpReplayDirectory.toString, s"${matchId}_compressed")

  def main(args: Array[String]): Unit = {
    if (args(0) == "analyze" || args(0) == "state") {
      val `match` = args(1)
      if (`match`.forall(_.isDigit)) {
        if (!Files.exists(cacheReplayPath(`match`))) {
          val replayLocation = StratzClient.getReplayLocation(`match`.toLong)
          replayLocation match {
            case Failure(_) => println(s"Can't find replay for match ${`match`}.")
            case Success(location) =>
              if (downloadReplay(location, compressedReplayPath(`match`), cacheReplayPath(`match`))) {
                if (args(0) == "analyze") analyze(cacheReplayPath(`match`)) else printCurrentState(cacheReplayPath(`match`))
              }
          }
        }
        else
          if (args(0) == "analyze") analyze(cacheReplayPath(`match`)) else printCurrentState(cacheReplayPath(`match`))
      }
      else {
        if (args(0) == "analyze") analyze(Paths.get(`match`)) else printCurrentState(Paths.get(`match`))
      }
    }
    else if (args(0) == "items") {
      runItemsAgainstHero(args(1))
    }
  }

  def printCurrentState(replay: Path): Unit = {
    val currentMapProcessor = new CurrentMapProcessor
    Using.Manager { use =>
      val source = use(new MappedFileSource(replay))(s => s.close())
      val runner = new SimpleRunner(source)
      runner.runWith(currentMapProcessor)
    }
  }

  def analyze(replay: Path): Unit = {
    println("Analyzing..")

    val analyzer = ReplayAnalyzer
    val result = analyzer.analyze(replay)
    val json = result.asJson

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
    result.abilityUsagesWithPT foreach { case (playerId, (total, onInt, manaLost)) =>
      println(s"${result.heroName(playerId)} ability usage with PT while farming: total $total, on Int: $onInt, mana lost: $manaLost (${manaLost / 150} clarities)")
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
    result.obsPlacedOnVision.foreach(obs =>
      println(s"${obs.created} ${result.heroName(obs.owner)}${if (obs.isFullDuration) " - not destroyed" else ""}")
    )

    if (result.unusedAbilities.nonEmpty) println("\nAbilities not used before death:")
    result.unusedAbilities.foreach { case (time, id, abilityId) =>
      println(s"${time.toString} ${result.heroName(id)} didn't use ${Abilities.getName(abilityId)}")
    }

    if (result.unusedOnAllyAbilities.nonEmpty) println("\nAbilities not used on ally:")
    result.unusedOnAllyAbilities foreach { case (time, deadPlayerId, allyId, abilityId) =>
      println(s"${time.toString} ${result.heroName(allyId)} didn't use ${Abilities.getName(abilityId)} for ${result.heroName(deadPlayerId)}")
    }

    if (result.unusedOnAllyWithBlinkAbilities.nonEmpty) println("\nAbilities not used with Blink Dagger on ally:")
    result.unusedOnAllyWithBlinkAbilities foreach { case (time, deadPlayerId, allyId, abilityId) =>
      println(s"${time.toString} ${result.heroName(allyId)} didn't use Blink Dagger + ${Abilities.getName(abilityId)} for ${result.heroName(deadPlayerId)}")
    }

    if (result.unusedItems.nonEmpty) println("\nItems not used before death:")
    result.unusedItems.foreach { case (time, id, itemId) =>
      println(s"${time.toString} ${result.heroName(id)} didn't use ${Items.getTag(itemId)}")
    }

    if (result.unusedOnAllyItems.nonEmpty) println("\nItems not used on ally:")
    result.unusedOnAllyItems foreach { case (time, deadPlayerId, allyId, itemId) =>
      println(s"${time.toString} ${result.heroName(allyId)} didn't use ${Items.getTag(itemId)} for ${result.heroName(deadPlayerId)}")
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

    val lostFightsUnderVisionByDire = result.fightsUnderVision.filter(f => f.fight.winner.contains(Radiant) && f.fight.dead.size >= 2 && f.getTeamWards(Dire).isEmpty)
    val lostFightsUnderVisionByRadiant = result.fightsUnderVision.filter(f => f.fight.winner.contains(Dire) && f.fight.dead.size >= 2 && f.getTeamWards(Radiant).isEmpty)

    if (lostFightsUnderVisionByDire.nonEmpty) println(s"\nLost fights under enemy vision by Dire:")
    lostFightsUnderVisionByDire.foreach(fight =>
      println(s"${fight.fight.start} - wards: ${fight.observers.map(obs => s"${obs.location} by ${result.heroName(obs.owner)}").mkString(", ")}")
    )

    if (lostFightsUnderVisionByRadiant.nonEmpty) println(s"\nLost fights under enemy vision by Radiant:")
    lostFightsUnderVisionByRadiant.foreach(fight =>
      println(s"${fight.fight.start} - wards: ${fight.observers.map(obs => s"${obs.location} by ${result.heroName(obs.owner)}").mkString(", ")}")
    )

    if (result.multipleRadiantLostFightsUnderWard.nonEmpty) println(s"\nMultiple lost fights under enemy vision by Radiant:")
    result.multipleRadiantLostFightsUnderWard.foreach { case (ward, fights) =>
      println(s"${fights.map(_.fight.start).mkString(", ")} - ward at ${ward.location} by ${result.heroName(ward.owner)}")
    }

    if (result.multipleDireLostFightsUnderWard.nonEmpty) println(s"\nMultiple lost fights under enemy vision by Dire:")
    result.multipleDireLostFightsUnderWard.foreach { case (ward, fights) =>
      println(s"${fights.map(_.fight.start).mkString(", ")} - ward at ${ward.location} by ${result.heroName(ward.owner)}")
    }

    if (result.unreasonableTeamDives.nonEmpty) println(s"\nUnreasonable team dives:")
    result.unreasonableTeamDives.foreach(fight =>
      println(s"${Util.getOppositeTeam(fight.winner.get)} dived at ${fight.start}")
    )

    if (result.unreasonableHeroDives.nonEmpty) println(s"\nUnreasonable hero dives:")
    result.unreasonableHeroDives.foreach { case (time, playerId, towerTier) =>
      println(s"${result.heroName(playerId)} dived at $time under enemy T$towerTier")
    }

    if (result.overlappedStuns.nonEmpty) println("\nOverlapped stuns:")
    result.overlappedStuns.foreach { case (time, stunnedId, attackerId, overlappedTime, abilityId) =>
      println(s"${result.heroName(attackerId)} stunned ${result.heroName(stunnedId)} too early at $time (ability is ${Abilities.getName(abilityId)}, overlapped time is $overlappedTime s.)")
    }

    if (result.mouseItemDelivery.nonEmpty) println("\nMouse click item delivery:")
    result.mouseItemDelivery.foreach { case (playerId, count) =>
      println(s"${result.heroName(playerId)} $count times")
    }

    if (result.mouseQuickBuy.nonEmpty) println("\nMouse click quick buy:")
    result.mouseQuickBuy.foreach { case (playerId, count) =>
      println(s"${result.heroName(playerId)} $count times")
    }

    if (result.notUnblockedCamps(Radiant)(Bot).nonEmpty) println("\nNot unblocked radiant bot easy camp:")
    result.notUnblockedCamps(Radiant)(Bot).foreach(ward =>
      println(s"at ${ward.created} by ${result.heroName(ward.owner)}")
    )

    if (result.notUnblockedCamps(Radiant)(Top).nonEmpty) println("\nNot unblocked radiant top hard camp:")
    result.notUnblockedCamps(Radiant)(Top).foreach(ward =>
      println(s"at ${ward.created} by ${result.heroName(ward.owner)}")
    )

    if (result.notUnblockedCamps(Dire)(Bot).nonEmpty) println("\nNot unblocked dire bottom hard camp:")
    result.notUnblockedCamps(Dire)(Bot).foreach(ward =>
      println(s"at ${ward.created} by ${result.heroName(ward.owner)}")
    )

    if (result.notUnblockedCamps(Dire)(Top).nonEmpty) println("\nNot unblocked dire top easy camp:")
    result.notUnblockedCamps(Dire)(Top).foreach(ward =>
      println(s"at ${ward.created} by ${result.heroName(ward.owner)}")
    )

    if (result.notPurchasedSticks.nonEmpty) println("\nNot purchased Sticks:")
    result.notPurchasedSticks.foreach { case (playerId, stickHeroId) =>
      println(s"${result.heroName(playerId)} didn't purchase Stick VS ${result.heroName(stickHeroId)}")
    }

    if (result.notPurchasedItemAgainstHero.nonEmpty) println("\nNot purchased items against heroes:")
    result.notPurchasedItemAgainstHero.foreach { case (heroId, itemName, noItemWinrate, itemWinrate, playerIds) =>
      println(s"${playerIds.map(id => result.heroName(id)).mkString(", ")} didn't buy $itemName VS ${Heroes.getName(heroId.id)} (no item winrate = $noItemWinrate%, item winrate = $itemWinrate%)")
    }

    if (result.unreactedLaneGanks.nonEmpty) println("\nUnreacted lane ganks:")
    result.unreactedLaneGanks.foreach { case (playerId, gankers, gankTime, gankLane) =>
      println(s"${result.heroName(playerId)} saw ${gankers.map(id => result.heroName(id)).mkString(", ")} coming on vision but didn't react to gank and died at $gankTime on $gankLane.")
    }
  }

  def runItemsAgainstHero(pathToMatches: String): Unit = {
    val source = scala.io.Source.fromFile(pathToMatches)
    val matches = try source.getLines().map(_.toLong).toSeq finally source.close()
    ItemAgainstHeroRunner.runWithStratz(matches)
  }

  def downloadReplay(location: ReplayLocation, compressedPath: Path, replayPath: Path): Boolean = {
    println(s"Downloading replay for match ${location.matchId} to $replayPath..")
    val isDownloaded = ValveClient.downloadReplay(location, compressedPath).isSuccess

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
