package wind

import skadistats.clarity.Clarity
import skadistats.clarity.processor.runner.SimpleRunner
import skadistats.clarity.source.MappedFileSource
import wind.models.Lane.Lane
import wind.models.Role.Role
import wind.models.Team.Team
import wind.models._
import wind.processors._

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
    val visionProcessor = new VisionProcessor
    val itemUsageProcessor = new ItemUsageProcessor
    val rolesProcessor = new RolesProcessor
    val abilityUsageProcessor = new AbilityUsageProcessor

    Using.Manager { use =>
      val source = use(new MappedFileSource(replay))(s => s.close())
      val runner = new SimpleRunner(source)
      runner.runWith(courierProcessor, heroProcessor, laneProcessor, powerTreadsProcessor, summonsProcessor,
        itemStockProcessor, glyphProcessor, visionProcessor, itemUsageProcessor, rolesProcessor, abilityUsageProcessor)
    }

    AnalysisResult(
      courierProcessor.courierIsOut.map { case (id, isOut) => PlayerId(id) -> isOut },
      laneProcessor.playerLane.map { case (id, lane) => PlayerId(id) -> lane },
      rolesProcessor.roles,
      laneProcessor.laneWinner,
      powerTreadsProcessor.abilityUsageCount.map { case (id, total) => PlayerId(id) -> (total, powerTreadsProcessor.ptOnIntAbilityUsageCount(id)) },
      powerTreadsProcessor.resourceItemUsages.map { case (id, total) => PlayerId(id) -> (total, powerTreadsProcessor.ptOnAgilityResourceItemUsages(id)) },
      summonsProcessor.summonFeedGold.map { case (id, gold) => PlayerId(id) -> gold },
      itemStockProcessor.maxSmokeStockDuration,
      itemStockProcessor.maxObsStockDuration,
      glyphProcessor.glyphNotUsedOnT1,
      visionProcessor.smokeUsedOnVision.map { case (id, times) => PlayerId(id)  -> times },
      visionProcessor.observerPlacedOnVision.map { case (id, times) => PlayerId(id)  -> times },
      heroProcessor.heroName.map { case(id, name) => PlayerId(id) -> name },
      itemUsageProcessor.deathsWithBKB,
      itemUsageProcessor.deathsWithEssenceRing,
      itemUsageProcessor.deathsWithMekansm,
      itemUsageProcessor.deathsWithGreaves,
      abilityUsageProcessor.unusedAbilities,

    )
  }
}

case class AnalysisResult(
  couriers: Map[PlayerId, Boolean],
  lanes: Map[PlayerId, (Lane, Lane)],
  roles: Map[PlayerId, Role],
  laneOutcomes: Map[Lane, Option[Team]],
  abilityUsagesWithPT: Map[PlayerId, (Int, Int)],
  resourceItemsUsagesWithPT: Map[PlayerId, (Int, Int)],
  goldFedWithSummons: Map[PlayerId, Int],
  maxStockSmokesDuration: Map[Team, Int],
  maxStockObsDuration: Map[Team, Int],
  glyphNotUsedOnT1: Map[Team, Int],
  smokesUsedOnVision: Map[PlayerId, List[GameTimeState]],
  obsPlacedOnVision: Map[PlayerId, List[GameTimeState]],
  heroName: Map[PlayerId, String],
  deathsWithBKB: Seq[(GameTimeState, PlayerId)],
  deathsWithEssenceRing: Seq[(GameTimeState, PlayerId)],
  deathsWithMekansm: Seq[(GameTimeState, PlayerId)],
  deathsWithGreaves: Seq[(GameTimeState, PlayerId)],
  unusedAbilities: Seq[(GameTimeState, PlayerId, String)],
)