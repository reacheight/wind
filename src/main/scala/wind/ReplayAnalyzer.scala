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
    val purchasesProcessor = new PurchasesProcessor
    val midasProcessor = new MidasEfficiencyProcessor
    val scanProcessor = new ScanProcessor
    val creepwaveProcessor = new CreepwaveProcessor
    val fightProcessor = new FightProcessor

    val start = System.currentTimeMillis()
    Using.Manager { use =>
      val source = use(new MappedFileSource(replay))(s => s.close())
      val runner = new SimpleRunner(source)
      runner.runWith(courierProcessor, heroProcessor, laneProcessor, powerTreadsProcessor, summonsProcessor,
        itemStockProcessor, glyphProcessor, visionProcessor, itemUsageProcessor, rolesProcessor, abilityUsageProcessor,
        purchasesProcessor, midasProcessor, scanProcessor, creepwaveProcessor, fightProcessor)
    }

    val badFightsProcessor = new BadFightsProcessor(fightProcessor.fights)
    Using.Manager { use =>
      val source = use(new MappedFileSource(replay))(s => s.close())
      val runner = new SimpleRunner(source)
      runner.runWith(badFightsProcessor)
    }

    println(s"${gameInfo.getGameInfo.getDota.getMatchId} analysis time: ${System.currentTimeMillis() - start} ms")

    AnalysisResult(
      gameInfo.getGameInfo.getDota.getMatchId,
      courierProcessor.courierIsOut.map { case (id, isOut) => PlayerId(id) -> isOut },
      laneProcessor.playerLane.map { case (id, lane) => PlayerId(id) -> lane },
      rolesProcessor.roles,
      laneProcessor.laneWinner,
      powerTreadsProcessor.abilityUsageCount.map { case (id, total) => PlayerId(id) -> (total, powerTreadsProcessor.ptOnIntAbilityUsageCount(id)) },
      powerTreadsProcessor.resourceItemUsages.map { case (id, total) => PlayerId(id) -> (total, powerTreadsProcessor.ptOnAgilityResourceItemUsages(id)) },
      powerTreadsProcessor.ptNotOnStrength,
      summonsProcessor.summonFeedGold.map { case (id, gold) => PlayerId(id) -> gold },
      itemStockProcessor.maxSmokeStockDuration,
      itemStockProcessor.maxObsStockDuration,
      glyphProcessor.glyphNotUsedOnT1,
      visionProcessor.smokeUsedOnVision,
      visionProcessor.observerPlacedOnVision,
      heroProcessor.heroName.map { case(id, name) => PlayerId(id) -> name },
      abilityUsageProcessor.unusedAbilities,
      abilityUsageProcessor.unusedOnAllyAbilities,
      itemUsageProcessor.unusedItems,
      purchasesProcessor.purchases,
      midasProcessor.midasEfficiency,
      scanProcessor.scanUsageCount,
      creepwaveProcessor.wastedCreepwaves,
      fightProcessor.fights,
      fightProcessor.fights.filter(fight => badFightsProcessor.badFights.contains(fight.start)),
    )
  }
}

case class AnalysisResult(
  matchId: Long,
  couriers: Map[PlayerId, Boolean],
  lanes: Map[PlayerId, (Lane, Lane)],
  roles: Map[PlayerId, Role],
  laneOutcomes: Map[Lane, Option[Team]],
  abilityUsagesWithPT: Map[PlayerId, (Int, Int)],
  resourceItemsUsagesWithPT: Map[PlayerId, (Int, Int)],
  ptNotOnStrength: Seq[(GameTimeState, PlayerId)],
  goldFedWithSummons: Map[PlayerId, Int],
  maxStockSmokesDuration: Map[Team, Int],
  maxStockObsDuration: Map[Team, Int],
  glyphNotUsedOnT1: Map[Team, Int],
  smokesUsedOnVision: Seq[(GameTimeState, PlayerId)],
  obsPlacedOnVision: Seq[(GameTimeState, PlayerId)],
  heroName: Map[PlayerId, String],
  unusedAbilities: Seq[(GameTimeState, PlayerId, String)],
  unusedOnAllyAbilities: Seq[(GameTimeState, PlayerId, PlayerId, String)],
  unusedItems: Seq[(GameTimeState, PlayerId, String)],
  purchases: Map[String, Seq[(String, Int)]],
  midasEfficiency: Map[PlayerId, Float],
  scanUsageCount: Map[Team, Int],
  wastedCreepwaves: Seq[(GameTimeState, Team, Lane, Int)],
  fights: Seq[Fight],
  badFights: Seq[Fight],
)