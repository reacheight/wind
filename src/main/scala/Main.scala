package wind

import processors._

import skadistats.clarity.Clarity
import skadistats.clarity.processor.runner.SimpleRunner
import skadistats.clarity.source.MappedFileSource

object Main {
  def main(args: Array[String]): Unit = {
    val runner = new SimpleRunner(new MappedFileSource(args(0)))
    val gameInfo = Clarity.infoForFile(args(0))

    val courierProcessor = new CourierProcessor
    val heroProcessor = new HeroProcessor(gameInfo)
    val laneProcessor = new LaneProcessor
    val powerTreadsProcessor = new PowerTreadsProcessor

    println(s"Parse started..")
    runner.runWith(courierProcessor, heroProcessor, laneProcessor, powerTreadsProcessor)
    println("Parse complete!\n")

    println("Couriers location at the start of the game:")
    courierProcessor.courierOutOfFountain.foreach(item => {
      val (playerId, outOfFountain) = item
      println(s"${heroProcessor.heroNameMap(playerId)} courier is ${if (outOfFountain) "out of" else "in"} fountain")
    })

    println("\nHeroes' lane stage location:")
    laneProcessor.heroLaneStageLocation.foreach(item => {
      val (playerId, location) = item
      println(s"${heroProcessor.heroNameMap(playerId)} lane stage location is $location (${laneProcessor.heroLaneMap(playerId)})")
    })

    println("\nChanging Power Treads' main attribute to Int before ability usage:")
    powerTreadsProcessor.powerTreadsAbilityUsageCount.foreach(item => {
      val (playerId, usageCount) = item
      println(s"${heroProcessor.heroNameMap(playerId)} power treads ability usage: total $usageCount, on Int: ${powerTreadsProcessor.powerTreadsOnIntAbilityUsageCount(playerId)}")
    })

    println("\nLane stage results:")
    laneProcessor.heroLaneStageExp.foreach(item => {
      val (playerId, exp) = item
      val heroName = heroProcessor.heroNameMap(playerId)
      println(s"$heroName exp: $exp, networth: ${laneProcessor.heroLaneStageNetworth(playerId)}")
    })
  }
}
