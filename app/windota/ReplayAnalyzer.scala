package windota

import com.typesafe.scalalogging.Logger
import skadistats.clarity.Clarity
import skadistats.clarity.processor.runner.SimpleRunner
import skadistats.clarity.source.MappedFileSource
import windota.models.Team._
import windota.models.Lane._
import windota.models.Role._
import windota.models._
import windota.processors._
import windota.processors.helpers.AbilityHelperProcessor

import java.nio.file.Path
import scala.util.Using

object ReplayAnalyzer {
  private val logger = Logger[ReplayAnalyzer.type]

  val OBS_VISION_RADIUS = 1400

  def analyze(replay: Path): AnalysisResultInternal = {
    val gameInfo = Clarity.infoForFile(replay.toAbsolutePath.toString)
    val game = gameInfo.getGameInfo.getDota

    val courierProcessor = new CourierProcessor
    val heroProcessor = new HeroProcessor(gameInfo)
    val laneProcessor = new LaneProcessor
    val powerTreadsProcessor = new PowerTreadsProcessor
    val summonsProcessor = new SummonsProcessor
    val itemStockProcessor = new ItemStockProcessor
    val glyphProcessor = new GlyphProcessor
    val visionProcessor = new VisionProcessor
    val itemUsageProcessor = new ItemUsageProcessor(true)
    val rolesProcessor = new RolesProcessor
    val abilityUsageProcessor = new AbilityUsageProcessor
    val purchasesProcessor = new PurchasesProcessor
    val midasProcessor = new MidasEfficiencyProcessor
    val scanProcessor = new ScanProcessor
    val creepwaveProcessor = new CreepwaveProcessor
    val fightProcessor = new FightProcessor
    val modifierProcessor = new ModifierProcessor
    val cursorProcessor = new CursorProcessor

    logger.info(s"Starting analysis for ${game.getMatchId}.")
    val start = System.currentTimeMillis()

    Using.Manager { use =>
      val source = use(new MappedFileSource(replay))(s => s.close())
      val runner = new SimpleRunner(source)
      try {
        runner.runWith(courierProcessor, heroProcessor, summonsProcessor,
          visionProcessor, itemUsageProcessor, abilityUsageProcessor,
          purchasesProcessor, midasProcessor, fightProcessor, modifierProcessor, creepwaveProcessor, cursorProcessor,
          laneProcessor, rolesProcessor, powerTreadsProcessor, new AbilityHelperProcessor
        )
      } catch {
        case e => logger.error(s"${e.getMessage}\n${e.getStackTrace.mkString("\n")}")
      }
    }

    val badFightsProcessor = new BadFightsProcessor(fightProcessor.fights)
    val smokeFightProcessor = new SmokeFightProcessor(fightProcessor.fights)
    val unreasonableDivesProcessor = new UnreasonableDivesProcessor(fightProcessor.fights)
    val itemBuildProcessor = new ItemBuildProcessor(rolesProcessor.roles)
    val unreactedLaneGanksProcessor = new UnreactedLaneGanksProcessor(fightProcessor.fights, laneProcessor.playerLane.map(pair => (PlayerId(pair._1), pair._2._1)))
    Using.Manager { use =>
      val source = use(new MappedFileSource(replay))(s => s.close())
      val runner = new SimpleRunner(source)

      try {
        runner.runWith(new ModifierProcessor, new HeroProcessor(gameInfo), badFightsProcessor, smokeFightProcessor,
          unreasonableDivesProcessor, itemBuildProcessor, new ItemUsageProcessor(false), unreactedLaneGanksProcessor
        )
      } catch {
        case e =>
          logger.error(s"${e.getMessage}\n${e.getStackTrace.mkString("\n")}")
      }
    }

    logger.info(s"Analysis finished for ${game.getMatchId}. Time: ${System.currentTimeMillis() - start} ms.")

    val notUnblockedCamps = new BlockedCampsProcessor()
      .getUnblockedCamps(visionProcessor.observers.appendedAll(visionProcessor.sentries), rolesProcessor.roles)

    val smokeOnVisionButWonFight = visionProcessor.smokeUsedOnVision.flatMap { case (smokeTime, playerId) =>
      val smokeTeam = if (Util.RadiantPlayerIds.contains(playerId)) Radiant else Dire
      smokeFightProcessor.smokeFights
        .find { case (smokeTimes, fight) => smokeTimes.get(smokeTeam).exists(_.gameTime == smokeTime.gameTime) && fight.winner.contains(smokeTeam) }
        .map { case (_, fight) => (fight.start, smokeTime, smokeTeam) }
    }

    val fightsUnderVision = fightProcessor.fights
      .map(fight => {
        val observersInFight = visionProcessor.observers
          .filter(obs => Util.getDistance(obs.location, fight.location) < OBS_VISION_RADIUS)
          .filter(obs => obs.created.gameTime < fight.start.gameTime && obs.ended.gameTime > fight.end.gameTime)

        FightUnderVision(fight, observersInFight)
      })
      .filter(_.observers.nonEmpty)

    def multipleLostFightsUnderOneWard(team: Team) = fightsUnderVision
      .filter(_.fight.winner.exists(_ == Util.getOppositeTeam(team)))
      .filter(_.getTeamWards(Util.getOppositeTeam(team)).nonEmpty)
      .groupBy(_.observers.head)
      .filter { case (_, fights) => fights.length >= 3 }
      .toSeq

    AnalysisResultInternal(
      game.getMatchId,
      game.getEndTime,
      game.getGameWinner == 2,
      courierProcessor.courierIsOut,
      laneProcessor.playerLane.map { case (id, lane) => PlayerId(id) -> lane },
      rolesProcessor.roles,
      laneProcessor.laneWinner,
      powerTreadsProcessor.abilityUsageCount.map { case (id, total) => PlayerId(id) -> (total, powerTreadsProcessor.ptOnIntAbilityUsageCount(id), powerTreadsProcessor.manaLostNoToggling(id)) },
      powerTreadsProcessor.resourceItemUsages.map { case (id, total) => PlayerId(id) -> (total, powerTreadsProcessor.ptOnAgilityResourceItemUsages(id)) },
      powerTreadsProcessor.ptNotOnStrength,
      summonsProcessor.summonFeedGold.map { case (id, gold) => PlayerId(id) -> gold },
      itemStockProcessor.maxSmokeStockDuration,
      itemStockProcessor.maxObsStockDuration,
      glyphProcessor.glyphNotUsedOnT1,
      glyphProcessor.glyphOnDeadT2,
      visionProcessor.smokeUsedOnVision,
      visionProcessor.observerPlacedOnVision,
      heroProcessor.heroName.map { case(id, name) => PlayerId(id) -> name },
      heroProcessor.heroId,
      abilityUsageProcessor.unusedAbilities,
      abilityUsageProcessor.unusedOnAllyAbilities,
      abilityUsageProcessor.unusedOnAllyWithBlinkAbilities,
      itemUsageProcessor.unusedItems,
      itemUsageProcessor.unusedOnAllyItems,
      purchasesProcessor.purchases,
      midasProcessor.midasEfficiency,
      scanProcessor.scanUsageCount,
      creepwaveProcessor.wastedCreepwaves,
      creepwaveProcessor.notTankedCreepwaves,
      fightProcessor.fights,
      badFightsProcessor.badFights,
      fightsUnderVision,
      multipleLostFightsUnderOneWard(Radiant),
      multipleLostFightsUnderOneWard(Dire),
      unreasonableDivesProcessor.unreasonableTeamDives,
      unreasonableDivesProcessor.unreasonableHeroDives,
      smokeFightProcessor.smokeFights,
      smokeOnVisionButWonFight,
      modifierProcessor.overlappedStuns,
      visionProcessor.observerPlacedOnVision.filter(obs => obs.isFullDuration),
      cursorProcessor.mouseClicksItemDelivery,
      cursorProcessor.mouseClicksQuickBuy,
      notUnblockedCamps,
      itemBuildProcessor.notPurchasedSticks,
      itemBuildProcessor.notPurchasedItemAgainstHero,
      unreactedLaneGanksProcessor.unreactedLaneGanks,
      modifierProcessor.scepter.toSeq,
      modifierProcessor.shard.toSeq
    )
  }
}

case class AnalysisResultInternal(
  matchId: Long,
  matchDuration: Int,
  radiantWon: Boolean,
  couriers: Map[PlayerId, (Boolean, Boolean)],
  lanes: Map[PlayerId, (Lane, Lane)],
  roles: Map[PlayerId, Role],
  laneOutcomes: Map[Lane, Option[Team]],
  abilityUsagesWithPT: Map[PlayerId, (Int, Int, Float)],
  resourceItemsUsagesWithPT: Map[PlayerId, (Int, Int)],
  ptNotOnStrength: Seq[(GameTimeState, PlayerId)],
  goldFedWithSummons: Map[PlayerId, Int],
  maxStockSmokesDuration: Map[Team, Int],
  maxStockObsDuration: Map[Team, Int],
  glyphNotUsedOnT1: Map[Team, Int],
  glyphOnDeadT2: Map[Team, Seq[GameTimeState]],
  smokesUsedOnVision: Seq[(GameTimeState, PlayerId)],
  obsPlacedOnVision: Seq[Ward],
  heroName: Map[PlayerId, String],
  heroId: Map[PlayerId, HeroId],
  unusedAbilities: Seq[(GameTimeState, PlayerId, Int)],
  unusedOnAllyAbilities: Seq[(GameTimeState, PlayerId, PlayerId, Int)],
  unusedOnAllyWithBlinkAbilities: Seq[(GameTimeState, PlayerId, PlayerId, Int)],
  unusedItems: Seq[(GameTimeState, PlayerId, Int)],
  unusedOnAllyItems: Seq[(GameTimeState, PlayerId, PlayerId, Int)],
  purchases: Map[String, Seq[(String, Int)]],
  midasEfficiency: Map[PlayerId, Float],
  scanUsageCount: Map[Team, Int],
  wastedCreepwaves: Seq[(GameTimeState, Team, Lane, Int)],
  notTankedCreepwaves: Seq[(GameTimeState, Team, Lane, Seq[PlayerId])],
  fights: Seq[Fight],
  badFights: Seq[BadFight],
  fightsUnderVision: Seq[FightUnderVision],
  multipleRadiantLostFightsUnderWard: Seq[(Ward, Seq[FightUnderVision])],
  multipleDireLostFightsUnderWard: Seq[(Ward, Seq[FightUnderVision])],
  unreasonableTeamDives: Seq[Fight],
  unreasonableHeroDives: Seq[(GameTimeState, PlayerId, Int)],
  smokeFights: Seq[(Map[Team, GameTimeState], Fight)],
  smokeOnVisionButWonFight: Seq[(GameTimeState, GameTimeState, Team)], // (fight start, smoke time, smoked team)
  overlappedStuns: Seq[internal.OverlappedStun], // (stun time, stunned player, attacker, overlapped time, ability id)
  obsesPlacedOnVisionButNotDestroyed: Seq[Ward],
  mouseItemDelivery: Seq[(PlayerId, Int)],
  mouseQuickBuy: Seq[(PlayerId, Int)],
  notUnblockedCamps: Map[PlayerId, Seq[Ward]],
  notPurchasedSticks: Seq[(PlayerId, PlayerId)],
  notPurchasedItemAgainstHero: Seq[(HeroId, String, Int, Int, Seq[PlayerId])],
  unreactedLaneGanks: Seq[(PlayerId, Seq[PlayerId], GameTimeState, Lane)],
  scepterOwners: Seq[PlayerId],
  shardOwners: Seq[PlayerId],
)