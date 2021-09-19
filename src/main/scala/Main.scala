package wind

import skadistats.clarity.processor.runner.SimpleRunner
import skadistats.clarity.source.MappedFileSource

object Main {
  def main(args: Array[String]): Unit = {
    val runner = new SimpleRunner(new MappedFileSource(args(0)))
    val courierProcessor = new CourierProcessor
    val heroProcessor = new HeroProcessor
    val laneProcessor = new LaneProcessor

    println(s"Parse started..")
    runner.runWith(courierProcessor, heroProcessor, laneProcessor)
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
  }
}
