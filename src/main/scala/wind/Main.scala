package wind

import skadistats.clarity.Clarity
import skadistats.clarity.processor.runner.SimpleRunner
import skadistats.clarity.source.MappedFileSource
import Team.{Dire, Radiant}
import wind.processors._

import java.nio.file.{Path, Paths}
import scala.util.Using

object Main {
  private val downloadingDirectory = Paths.get("tmp")
  private val compressedReplayPath = Paths.get(downloadingDirectory.toString, "replay.dem.bz2")
  private val replayPath = Paths.get(downloadingDirectory.toString, "replay.dem")

  def main(args: Array[String]): Unit = {
    val matchId = args(0)
    val replayLocation = OdotaClient.getReplayLocation(matchId)
    replayLocation match {
      case None => println(s"Can't find replay for match $matchId.")
      case Some(location) =>
        println("Downloading replay...")
        ReplayDownloader.downloadReplay(location, compressedReplayPath)

        println("Decompressing replay..")
        BZip2Decompressor.decompress(compressedReplayPath, replayPath)

        println("Analyzing replay...")
        analyze(replayPath)

        compressedReplayPath.toFile.delete()
        replayPath.toFile.delete()
        downloadingDirectory.toFile.delete()
    }
  }

  def analyze(replay: Path): Unit = {
    val gameInfo = Clarity.infoForFile(replay.toAbsolutePath.toString)
    val courierProcessor = new CourierProcessor
    val heroProcessor = new HeroProcessor(gameInfo)
    val laneProcessor = new LaneProcessor
    val powerTreadsProcessor = new PowerTreadsProcessor
    val summonsProcessor = new SummonsProcessor
    val itemStockProcessor = new ItemStockProcessor
    val glyphProcessor = new GlyphProcessor

    Using.Manager { use =>
      val source = use(new MappedFileSource(replay))(s => s.close())
      val runner = new SimpleRunner(source)
      runner.runWith(courierProcessor, heroProcessor, laneProcessor, powerTreadsProcessor, summonsProcessor, itemStockProcessor, glyphProcessor)
    }

    println("Couriers location at the start of the game:")
    courierProcessor.courierOutOfFountain foreach {case (playerId, outOfFountain) =>
      println(s"${heroProcessor.heroNameMap(playerId)} courier is ${if (outOfFountain) "out of" else "in"} fountain")
    }

    println("\nHeroes' lane stage location:")
    laneProcessor.laneStageLocation foreach {case (playerId, (firstStageLocation, secondStageLocation)) =>
      val (firstStageLane, secondStageLane) = laneProcessor.playerLane(playerId)

      println(s"${heroProcessor.heroNameMap(playerId)} lane stage location is $firstStageLocation -> $secondStageLocation " +
        s"($firstStageLane -> $secondStageLane)")
    }

    println("\nChanging Power Treads' main attribute to Int before ability usage:")
    powerTreadsProcessor.abilityUsageCount foreach {case (playerId, usageCount) =>
      println(s"${heroProcessor.heroNameMap(playerId)} power treads ability usage: total $usageCount, on Int: ${powerTreadsProcessor.ptOnIntAbilityUsageCount(playerId)}")
    }

    println("\nChanging Power Treads' main attribute to Agility before resource refill:")
    powerTreadsProcessor.resourceItemUsages foreach {case (playerId, usages) =>
      println(s"${heroProcessor.heroNameMap(playerId)} power treads resource refill item usage: total $usages, on Agility: ${powerTreadsProcessor.ptOnAgilityResourceItemUsages(playerId)}")
    }

    println("\nLane stage results:")
    laneProcessor.laneExp foreach {case (playerId, exp) =>
      val heroName = heroProcessor.heroNameMap(playerId)
      println(s"$heroName exp: $exp, networth: ${laneProcessor.laneNetworth(playerId)}")
    }

    println()
    laneProcessor.laneWinner foreach {case (lane, winner) =>
      println(s"$lane winner: ${winner.getOrElse("Draw")}")
    }

    println("\nSummon gold fed:")
    summonsProcessor.summonFeedGold foreach {case (playerId, gold) =>
      val heroName = heroProcessor.heroNameMap(playerId)
      println(s"$heroName: $gold")
    }

    println("\nMax smoke stock duration:")
    println(s"Radiant: ${itemStockProcessor.maxSmokeStockDuration(Radiant)} sec.")
    println(s"Dire: ${itemStockProcessor.maxSmokeStockDuration(Dire)} sec.")

    println("\nMax obs stock duration:")
    println(s"Radiant: ${itemStockProcessor.maxObsStockDuration(Radiant)} sec.")
    println(s"Dire: ${itemStockProcessor.maxObsStockDuration(Dire)} sec.")

    println("\nGlyph not used on T1 count:")
    println(s"Radiant: ${glyphProcessor.glyphNotUsedOnT1.getOrElse(Radiant, 0)}")
    println(s"Dire: ${glyphProcessor.glyphNotUsedOnT1.getOrElse(Dire, 0)}")
  }
}
