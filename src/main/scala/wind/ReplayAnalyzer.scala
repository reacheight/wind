package wind

import com.typesafe.scalalogging.Logger
import skadistats.clarity.Clarity
import skadistats.clarity.processor.runner.SimpleRunner
import skadistats.clarity.source.MappedFileSource
import wind.models.Lane.Lane
import wind.models.Role.Role
import wind.models.Team.{Dire, Radiant, Team}
import wind.models._
import wind.processors._

import java.nio.file.Path
import scala.util.Using

object ReplayAnalyzer {
  val logger = Logger[ReplayAnalyzer.type]

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
    val itemUsageProcessor = new ItemUsageProcessor
    val rolesProcessor = new RolesProcessor
    val abilityUsageProcessor = new AbilityUsageProcessor
    val purchasesProcessor = new PurchasesProcessor
    val midasProcessor = new MidasEfficiencyProcessor
    val scanProcessor = new ScanProcessor
    val creepwaveProcessor = new CreepwaveProcessor
    val fightProcessor = new FightProcessor
    val modifierProcessor = new ModifierProcessor

    logger.info(s"starting analysis for ${game.getMatchId}")
    val start = System.currentTimeMillis()

    Using.Manager { use =>
      val source = use(new MappedFileSource(replay))(s => s.close())
      val runner = new SimpleRunner(source)
      runner.runWith(courierProcessor, heroProcessor, summonsProcessor,
        glyphProcessor, visionProcessor, itemUsageProcessor, abilityUsageProcessor,
        purchasesProcessor, midasProcessor, fightProcessor, modifierProcessor, creepwaveProcessor)
    }

    val badFightsProcessor = new BadFightsProcessor(fightProcessor.fights)
    val smokeFightProcessor = new SmokeFightProcessor(fightProcessor.fights)
    val unreasonableDivesProcessor = new UnreasonableDivesProcessor(fightProcessor.fights)
    Using.Manager { use =>
      val source = use(new MappedFileSource(replay))(s => s.close())
      val runner = new SimpleRunner(source)
      runner.runWith(new ModifierProcessor, new HeroProcessor(gameInfo), badFightsProcessor, smokeFightProcessor, unreasonableDivesProcessor)
    }

    logger.info(s"${game.getMatchId} analysis time: ${System.currentTimeMillis() - start} ms")

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
      powerTreadsProcessor.abilityUsageCount.map { case (id, total) => PlayerId(id) -> (total, powerTreadsProcessor.ptOnIntAbilityUsageCount(id)) },
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
      unreasonableDivesProcessor.unreasonableDives,
      smokeFightProcessor.smokeFights,
      smokeOnVisionButWonFight,
      modifierProcessor.overlappedStuns,
      visionProcessor.observerPlacedOnVision.filter(obs => obs.isFullTime),
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
  abilityUsagesWithPT: Map[PlayerId, (Int, Int)],
  resourceItemsUsagesWithPT: Map[PlayerId, (Int, Int)],
  ptNotOnStrength: Seq[(GameTimeState, PlayerId)],
  goldFedWithSummons: Map[PlayerId, Int],
  maxStockSmokesDuration: Map[Team, Int],
  maxStockObsDuration: Map[Team, Int],
  glyphNotUsedOnT1: Map[Team, Int],
  glyphOnDeadT2: Map[Team, Seq[GameTimeState]],
  smokesUsedOnVision: Seq[(GameTimeState, PlayerId)],
  obsPlacedOnVision: Seq[Observer],
  heroName: Map[PlayerId, String],
  heroId: Map[PlayerId, HeroId],
  unusedAbilities: Seq[(GameTimeState, PlayerId, String)],
  unusedOnAllyAbilities: Seq[(GameTimeState, PlayerId, PlayerId, String)],
  unusedOnAllyWithBlinkAbilities: Seq[(GameTimeState, PlayerId, PlayerId, String)],
  unusedItems: Seq[(GameTimeState, PlayerId, String)],
  unusedOnAllyItems: Seq[(GameTimeState, PlayerId, PlayerId, String)],
  purchases: Map[String, Seq[(String, Int)]],
  midasEfficiency: Map[PlayerId, Float],
  scanUsageCount: Map[Team, Int],
  wastedCreepwaves: Seq[(GameTimeState, Team, Lane, Int)],
  notTankedCreepwaves: Seq[(GameTimeState, Team, Lane, Seq[PlayerId])],
  fights: Seq[Fight],
  badFights: Seq[BadFight],
  fightsUnderVision: Seq[FightUnderVision],
  multipleRadiantLostFightsUnderWard: Seq[(Observer, Seq[FightUnderVision])],
  multipleDireLostFightsUnderWard: Seq[(Observer, Seq[FightUnderVision])],
  unreasonableDives: Seq[Fight],
  smokeFights: Seq[(Map[Team, GameTimeState], Fight)],
  smokeOnVisionButWonFight: Seq[(GameTimeState, GameTimeState, Team)], // (fight start, smoke time, smoked team)
  overlappedStuns: Seq[(GameTimeState, PlayerId, PlayerId)], // (stun time, stunned player, attacker)
  obsesPlacedOnVisionButNotDestroyed: Seq[Observer],
)