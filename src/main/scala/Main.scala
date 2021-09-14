package wind

import skadistats.clarity.processor.runner.SimpleRunner
import skadistats.clarity.source.MappedFileSource

object Main {
  def main(args: Array[String]): Unit = {
    val runner = new SimpleRunner(new MappedFileSource(args(0)))
    val courierProcessor = new CourierProcessor

    println(s"Parse started with ${courierProcessor.getClass.getSimpleName}...")
    runner.runWith(courierProcessor)
    println("Parse complete!\n")

    println("Couriers location at the start of the game:")
    courierProcessor.courierOutOfFountain.foreach(item => {
      val (playerId, outOfFountain) = item
      println(s"${courierProcessor.heroMap(playerId)} courier is ${if (outOfFountain) "out of" else "in"} fountain")
    })
  }
}
