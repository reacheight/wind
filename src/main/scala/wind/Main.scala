package wind

import skadistats.clarity.Clarity
import skadistats.clarity.processor.runner.SimpleRunner
import skadistats.clarity.source.MappedFileSource
import Team.{Dire, Radiant}
import wind.processors._

import java.nio.file.{Path, Paths}

object Main {
  private val compressedReplayPath = Paths.get("tmp/replay.dem.bz2")
  private val replayPath = Paths.get("tmp/replay.dem")

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
    }
  }

  def analyze(replay: Path): Unit = {
    val runner = new SimpleRunner(new MappedFileSource(replay))
    val gameInfo = Clarity.infoForFile(replay.toAbsolutePath.toString)

    val courierProcessor = new CourierProcessor
    val heroProcessor = new HeroProcessor(gameInfo)
    val laneProcessor = new LaneProcessor
    val powerTreadsProcessor = new PowerTreadsProcessor
    val summonsProcessor = new SummonsProcessor
    val itemStockProcessor = new ItemStockProcessor
    runner.runWith(courierProcessor, heroProcessor, laneProcessor, powerTreadsProcessor, summonsProcessor, itemStockProcessor)

    println("Couriers location at the start of the game:")
    courierProcessor.courierOutOfFountain.foreach(item => {
      val (playerId, outOfFountain) = item
      println(s"${heroProcessor.heroNameMap(playerId)} courier is ${if (outOfFountain) "out of" else "in"} fountain")
    })

    println("\nHeroes' lane stage location:")
    laneProcessor.heroLaneStageLocation.foreach(item => {
      val (playerId, locations) = item
      val (firstStageLocation, secondStageLocation) = locations
      val (firstStageLane, secondStageLane) = laneProcessor.heroLaneMap(playerId)

      println(s"${heroProcessor.heroNameMap(playerId)} lane stage location is $firstStageLocation -> $secondStageLocation " +
        s"($firstStageLane -> $secondStageLane)")
    })

    println("\nChanging Power Treads' main attribute to Int before ability usage:")
    powerTreadsProcessor.abilityUsageCount.foreach(item => {
      val (playerId, usageCount) = item
      println(s"${heroProcessor.heroNameMap(playerId)} power treads ability usage: total $usageCount, on Int: ${powerTreadsProcessor.ptOnIntAbilityUsageCount(playerId)}")
    })

    println("\nChanging Power Treads' main attribute to Agility before resource refill:")
    powerTreadsProcessor.resourceItemUsages.foreach(item => {
      val (playerId, usages) = item
      println(s"${heroProcessor.heroNameMap(playerId)} power treads resource refill item usage: total $usages, on Agility: ${powerTreadsProcessor.ptOnAgilityResourceItemUsages(playerId)}")
    })

    println("\nLane stage results:")
    laneProcessor.heroLaneStageExp.foreach(item => {
      val (playerId, exp) = item
      val heroName = heroProcessor.heroNameMap(playerId)
      println(s"$heroName exp: $exp, networth: ${laneProcessor.heroLaneStageNetworth(playerId)}")
    })

    println("\nSummon gold fed:")
    summonsProcessor.summonFeedGold.foreach(item => {
      val (playerId, gold) = item
      val heroName = heroProcessor.heroNameMap(playerId)
      println(s"$heroName: $gold")
    })

    println("\nMax smoke stock duration:")
    println(s"Radiant: ${itemStockProcessor.maxSmokeStockDuration(Radiant)} sec.")
    println(s"Dire: ${itemStockProcessor.maxSmokeStockDuration(Dire)} sec.")

    println("\nMax obs stock duration:")
    println(s"Radiant: ${itemStockProcessor.maxObsStockDuration(Radiant)} sec.")
    println(s"Dire: ${itemStockProcessor.maxObsStockDuration(Dire)} sec.")
  }
}
