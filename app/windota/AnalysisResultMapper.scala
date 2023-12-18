package windota

import windota.models.Team._
import windota.models.dto.{DeathSummary, PlayerHero, UnusedAbility, UnusedItem}
import windota.models.{internal, _}

object AnalysisResultMapper {
  def toJsonModel(analysisResult: AnalysisResultInternal): AnalysisResult = {
    def heroId(playerId: PlayerId) = analysisResult.heroId(playerId)

    val overlappedStuns = analysisResult.overlappedStuns.map(stun => OverlappedStun(heroId(stun.attacker), heroId(stun.target), stun.time, stun.overlapTime, stun.stunSourceId, stun.isAbility))
    val courierStates = analysisResult.couriers.map { case (playerId, (isOut, isVersusMK)) => CourierState(heroId(playerId), isOut, isVersusMK) }.toSeq
    val notTankedCreepwave = analysisResult.notTankedCreepwaves.map { case (time, _, lane, players) => NotTankedCreepwave(players.map(heroId), lane, time) }
    val summonGoldFed = analysisResult.goldFedWithSummons.map { case (playerId, gold) => SummonGoldFed(heroId(playerId), gold) }.toSeq
    val midasEfficiency = analysisResult.midasEfficiency.map { case (playerId, efficiency) => MidasEfficiency(heroId(playerId), efficiency) }.toSeq
    val observersOnVision = analysisResult.obsPlacedOnVision.map(obs => ObserverOnVision(heroId(obs.owner), obs.created, !obs.isFullDuration))
    val smokesOnVision = analysisResult.smokesUsedOnVision.map { case (time, playerId) => SmokeOnVision(heroId(playerId), time) }
    val badFights = analysisResult.badFights.map(fight => BadFightJsonModel(fight.fight.outnumberedTeam.get, fight.seenPlayers.map { case (id, loc) => SeenHero(heroId(id), loc) }.toSeq, fight.fight.start))
    val badSmokeFights = analysisResult.smokeOnVisionButWonFight.map { case (fightStart, smokeTime, smokedTeam) => BadSmokeFight(smokedTeam, smokeTime, fightStart) }
    val worthlessGlyphs = analysisResult.glyphOnDeadT2.map { case (team, glyphs) => WorthlessGlyph(team, glyphs) }.filter(_.times.nonEmpty).toSeq
    val lostFightsUnderTheSameWard = analysisResult.multipleRadiantLostFightsUnderWard.map { case (observer, fights) => LostFightsUnderTheSameWard(Radiant, fights.map(_.fight.start), heroId(observer.owner))} ++
      analysisResult.multipleDireLostFightsUnderWard.map { case (observer, fights) => LostFightsUnderTheSameWard(Dire, fights.map(_.fight.start), heroId(observer.owner))}

    val fightsLostUnderEnemyVision = analysisResult.fightsUnderVision.filter(f => f.fight.winner.exists(winner => f.getTeamWards(Util.getOppositeTeam(winner)).isEmpty))
      .map(f => FightLostUnderEnemyVision(Util.getOppositeTeam(f.fight.winner.get), f.fight.start))

    val unreasonableTeamDives = analysisResult.unreasonableTeamDives.map(fight => UnreasonableTeamDive(Util.getOppositeTeam(fight.winner.get), fight.start))
    val mouseClickItemDeliveries = analysisResult.mouseItemDelivery.map { case (playerId, count) => MouseClickItemDelivery(heroId(playerId), count) }
    val mouseClickQuickBuys = analysisResult.mouseQuickBuy.map { case (playerId, count) => MouseClickQuickBuy(heroId(playerId), count) }
    val notUnblockedCamps = analysisResult.notUnblockedCamps.map { case (responsible, wards) => NotUnblockedCamp(heroId(responsible), wards.map(w => CampBlock(heroId(w.owner), w.created))) }.toSeq
      .filter(c => c.blocks.nonEmpty)

    val unreasonableHeroDives = analysisResult.unreasonableHeroDives.map { case (time, player, towerTier) => UnreasonableHeroDive(heroId(player), time, towerTier) }

    val notPurchasedSticks = analysisResult.notPurchasedSticks.map { case(playerId, stickPlayerId) => NotPurchasedStick(heroId(playerId), heroId(stickPlayerId)) }
    val notPurchasedItemAgainstHero = analysisResult.notPurchasedItemAgainstHero.map { case (hero, itemName, noItemWinrate, itemWinrate, candidates) =>
      NotPurchasedItemAgainstHero(hero, itemName, noItemWinrate, itemWinrate, candidates.map(heroId))
    }

    val powerTreadsAbilityUsages = analysisResult.abilityUsagesWithPT.filter(_._2._3 > 150).map { case (playerId, (total, onInt, manaLost)) => PowerTreadsAbilityUsages(heroId(playerId), total, onInt, manaLost)}.toSeq

//    val unreactedLaneGanks = analysisResult.unreactedLaneGanks.map { case(target, gankers, time, lane) => UnreactedLaneGank(heroId(target), gankers.map(heroId), time, lane) }

    val scepterOwners = analysisResult.scepterOwners.map(heroId)
    val shardOwners = analysisResult.shardOwners.map(heroId)

    val analysis = Analysis(
      analysisResult.unusedItems.map(d => UnusedItem.fromInternal(d, heroId)),
      analysisResult.unusedAbilities.map(d => UnusedAbility.fromInternal(d, heroId)),
      overlappedStuns,
      midasEfficiency,
      courierStates,
      notTankedCreepwave,
      notUnblockedCamps,
      observersOnVision,
      smokesOnVision,
      badFights,
      badSmokeFights,
      lostFightsUnderTheSameWard,
      unreasonableTeamDives,
//      unreactedLaneGanks,
      unreasonableHeroDives,
      summonGoldFed,
      worthlessGlyphs,
      fightsLostUnderEnemyVision,
      mouseClickItemDeliveries,
      mouseClickQuickBuys,
      notPurchasedSticks,
      notPurchasedItemAgainstHero,
      powerTreadsAbilityUsages,
      unreasonableHeroDives,
      scepterOwners,
      shardOwners,
      analysisResult.deathsSummary.map(d => DeathSummary.fromInternal(d, heroId))
    )

    val radiant = analysisResult.heroId.filter(_._1.id < 10).values.toSeq
    val dire = analysisResult.heroId.filter(_._1.id >= 10).values.toSeq
    val matchInfo = MatchInfo(
      radiant,
      dire,
      analysisResult.radiantWon,
      analysisResult.matchDuration,
      analysisResult.heroId.map { case (playerId, heroId) => PlayerHero(playerId, heroId) }.toSeq
    )

    AnalysisResult(matchInfo, analysis)
  }
}
