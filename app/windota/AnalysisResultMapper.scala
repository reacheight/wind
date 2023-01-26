package windota

import windota.models.Team._
import windota.models._

object AnalysisResultMapper {
  def toJsonModel(analysisResult: AnalysisResultInternal): AnalysisResult = {
    def heroId(playerId: PlayerId) = analysisResult.heroId(playerId)

    val unusedItems = (analysisResult.unusedItems.map { case (time, id, item) => UnusedItem(heroId(id), heroId(id), ItemId(item), time) } ++
      analysisResult.unusedOnAllyItems.map { case (time, target, user, item) => UnusedItem(heroId(user), heroId(target), ItemId(item), time) })
        .sortBy(e => e.time.gameTime)

    val unusedAbilities = (analysisResult.unusedAbilities.map { case (time, id, ability) => UnusedAbility(heroId(id), heroId(id), AbilityId(ability), time) } ++
      analysisResult.unusedOnAllyAbilities.map { case (time, target, user, ability) => UnusedAbility(heroId(user), heroId(target), AbilityId(ability), time) } ++
      analysisResult.unusedOnAllyWithBlinkAbilities.map { case (time, target, user, ability) => UnusedAbility(heroId(user), heroId(target), AbilityId(ability), time, withBlink = true) })
        .sortBy(e => e.time.gameTime)

    val overlappedStuns = analysisResult.overlappedStuns.map { case (time, target, user, overlappedTime, abilityId) => OverlappedStun(heroId(user), heroId(target), time, overlappedTime, AbilityId(abilityId)) }
    val courierStates = analysisResult.couriers.map { case (playerId, (isOut, isVersusMK)) => CourierState(heroId(playerId), isOut, isVersusMK) }.toSeq
    val notTankedCreepwave = analysisResult.notTankedCreepwaves.map { case (time, _, lane, players) => NotTankedCreepwave(players.map(heroId), lane, time) }
    val summonGoldFed = analysisResult.goldFedWithSummons.map { case (playerId, gold) => SummonGoldFed(heroId(playerId), gold) }.toSeq
    val midasEfficiency = analysisResult.midasEfficiency.map { case (playerId, efficiency) => MidasEfficiency(heroId(playerId), efficiency) }.toSeq
    val observersOnVision = analysisResult.obsPlacedOnVision.map(obs => ObserverOnVision(heroId(obs.owner), obs.created, !obs.isFullDuration))
    val smokesOnVision = analysisResult.smokesUsedOnVision.map { case (time, playerId) => SmokeOnVision(heroId(playerId), time) }
    val badFights = analysisResult.badFights.map(fight => BadFightJsonModel(fight.fight.outnumberedTeam.get, fight.seenPlayers.map(heroId).toSeq, fight.fight.start))
    val badSmokeFights = analysisResult.smokeOnVisionButWonFight.map { case (fightStart, smokeTime, smokedTeam) => BadSmokeFight(smokedTeam, smokeTime, fightStart) }
    val worthlessGlyphs = analysisResult.glyphOnDeadT2.map { case (team, glyphs) => WorthlessGlyph(team, glyphs) }.filter(_.times.nonEmpty).toSeq
    val lostFightsUnderTheSameWard = analysisResult.multipleRadiantLostFightsUnderWard.map { case (observer, fights) => LostFightsUnderTheSameWard(Radiant, fights.map(_.fight.start), heroId(observer.owner))} ++
      analysisResult.multipleDireLostFightsUnderWard.map { case (observer, fights) => LostFightsUnderTheSameWard(Dire, fights.map(_.fight.start), heroId(observer.owner))}

    val fightsLostUnderEnemyVision = analysisResult.fightsUnderVision.filter(f => f.fight.winner.exists(winner => f.getTeamWards(Util.getOppositeTeam(winner)).isEmpty))
      .map(f => FightLostUnderEnemyVision(Util.getOppositeTeam(f.fight.winner.get), f.fight.start))

    val unreasonableDives = analysisResult.unreasonableTeamDives.map(fight => UnreasonableDive(Util.getOppositeTeam(fight.winner.get), fight.start))
    val mouseClickItemDeliveries = analysisResult.mouseItemDelivery.map { case (playerId, count) => MouseClickItemDelivery(heroId(playerId), count) }
    val mouseClickQuickBuys = analysisResult.mouseQuickBuy.map { case (playerId, count) => MouseClickQuickBuy(heroId(playerId), count) }
    val notUnblockedCamps = analysisResult.notUnblockedCamps.flatMap { case (team, camps) => camps.map { case (lane, wards) => NotUnblockedCamp(team, lane, wards.map(w => w.created))} }.toSeq
      .filter(c => c.blocks.nonEmpty)

    val notPurchasedSticks = analysisResult.notPurchasedSticks.map { case(playerId, stickPlayerId) => NotPurchasedStick(heroId(playerId), heroId(stickPlayerId)) }
    val notPurchasedItemAgainstHero = analysisResult.notPurchasedItemAgainstHero.map { case (hero, itemName, noItemWinrate, itemWinrate, candidates) =>
      NotPurchasedItemAgainstHero(hero, itemName, noItemWinrate, itemWinrate, candidates.map(heroId))
    }

    val powerTreadsAbilityUsages = analysisResult.abilityUsagesWithPT.filter(_._2._3 > 150).map { case (playerId, (total, onInt, manaLost)) => PowerTreadsAbilityUsages(heroId(playerId), total, onInt, manaLost)}.toSeq

    val analysis = Analysis(
      unusedItems,
      unusedAbilities,
      overlappedStuns,
      courierStates,
      notTankedCreepwave,
      summonGoldFed,
      midasEfficiency,
      observersOnVision,
      smokesOnVision,
      badFights,
      badSmokeFights,
      worthlessGlyphs,
      lostFightsUnderTheSameWard,
      fightsLostUnderEnemyVision,
      unreasonableDives,
      mouseClickItemDeliveries,
      mouseClickQuickBuys,
      notUnblockedCamps,
      notPurchasedSticks,
      notPurchasedItemAgainstHero,
      powerTreadsAbilityUsages,
    )

    val radiant = analysisResult.heroId.filter(_._1.id < 10).values.toSeq
    val dire = analysisResult.heroId.filter(_._1.id >= 10).values.toSeq
    val matchInfo = MatchInfo(
      radiant,
      dire,
      analysisResult.radiantWon,
      analysisResult.matchDuration
    )

    AnalysisResult(matchInfo, analysis)
  }
}
