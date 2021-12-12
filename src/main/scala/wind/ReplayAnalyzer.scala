package wind

import skadistats.clarity.Clarity
import skadistats.clarity.processor.runner.SimpleRunner
import skadistats.clarity.source.MappedFileSource
import wind.Lane.Lane
import wind.Team.Team
import wind.processors.{CourierProcessor, GlyphProcessor, HeroProcessor, ItemStockProcessor, LaneProcessor, PowerTreadsProcessor, SummonsProcessor, VisionProcessor, WinProbabilityProcessor}

import java.nio.file.Path
import scala.util.Using

object ReplayAnalyzer {
  def analyze(replay: Path): AnalysisResult = {
    val gameInfo = Clarity.infoForFile(replay.toAbsolutePath.toString)
    val courierProcessor = new CourierProcessor
    val heroProcessor = new HeroProcessor(gameInfo)
    val laneProcessor = new LaneProcessor
    val powerTreadsProcessor = new PowerTreadsProcessor
    val summonsProcessor = new SummonsProcessor
    val itemStockProcessor = new ItemStockProcessor
    val glyphProcessor = new GlyphProcessor
    val winProbabilityProcessor = new WinProbabilityProcessor
    val visionProcessor = new VisionProcessor

    Using.Manager { use =>
      val source = use(new MappedFileSource(replay))(s => s.close())
      val runner = new SimpleRunner(source)
      runner.runWith(courierProcessor, heroProcessor, laneProcessor, powerTreadsProcessor, summonsProcessor,
        itemStockProcessor, glyphProcessor, winProbabilityProcessor, visionProcessor)
    }

    AnalysisResult(
      courierProcessor.courierIsOut.map { case (id, isOut) => PlayerId(id) -> isOut },
      laneProcessor.playerLane.map { case (id, lane) => PlayerId(id) -> lane },
      laneProcessor.laneWinner,
      powerTreadsProcessor.abilityUsageCount.map { case (id, total) => PlayerId(id) -> (total, powerTreadsProcessor.ptOnIntAbilityUsageCount(id)) },
      powerTreadsProcessor.resourceItemUsages.map { case (id, total) => PlayerId(id) -> (total, powerTreadsProcessor.ptOnAgilityResourceItemUsages(id)) },
      summonsProcessor.summonFeedGold.map { case (id, gold) => PlayerId(id) -> gold },
      itemStockProcessor.maxSmokeStockDuration,
      itemStockProcessor.maxObsStockDuration,
      glyphProcessor.glyphNotUsedOnT1,
      visionProcessor.smokeUsedOnVision.map { case (id, time) => PlayerId(id)  -> time },
      visionProcessor.observerPlacedOnVision.map { case (id, time) => PlayerId(id)  -> time },
      heroProcessor.heroName.map { case(id, name) => PlayerId(id) -> name }
    )
  }
}

case class AnalysisResult(
  couriers: Map[PlayerId, Boolean],
  lanes: Map[PlayerId, (Lane, Lane)],
  laneOutcomes: Map[Lane, Option[Team]],
  abilityUsagesWithPT: Map[PlayerId, (Int, Int)],
  resourceItemsUsagesWithPT: Map[PlayerId, (Int, Int)],
  goldFedWithSummons: Map[PlayerId, Int],
  maxStockSmokesDuration: Map[Team, Int],
  maxStockObsDuration: Map[Team, Int],
  glyphNotUsedOnT1: Map[Team, Int],
  smokesUsedOnVision: List[(PlayerId, GameTimeState)],
  obsPlacedOnVision: List[(PlayerId, GameTimeState)],
  heroName: Map[PlayerId, String]
)

case class PlayerId(id: Int)