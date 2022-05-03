package wind

import wind.models._

object AnalysisResultMapper {
  def toJsonModel(analysisResult: AnalysisResultInternal): AnalysisResult = {
    def heroId(playerId: PlayerId) = analysisResult.heroId(playerId)

    val unusedItems = analysisResult.unusedItems.map { case (time, id, item) => UnusedItem(heroId(id), heroId(id), item, time) } ++
      analysisResult.unusedOnAllyItems.map { case (time, target, user, item) => UnusedItem(heroId(user), heroId(target), item, time) }


    val unusedAbilities = analysisResult.unusedAbilities.map { case (time, id, ability) => UnusedAbility(heroId(id), heroId(id), ability, time) } ++
      analysisResult.unusedOnAllyAbilities.map { case (time, target, user, ability) => UnusedAbility(heroId(user), heroId(target), ability, time) } ++
      analysisResult.unusedOnAllyWithBlinkAbilities.map { case (time, target, user, ability) => UnusedAbility(heroId(user), heroId(target), ability, time, withBlink = true) }

    val overlappedStuns = analysisResult.overlappedStuns.map { case (time, target, user) => OverlappedStun(heroId(user), heroId(target), time) }
    val courierStates = analysisResult.couriers.map { case (playerId, (isOut, isVersusMK)) => CourierState(heroId(playerId), isOut, isVersusMK) }.toSeq
    val notTankedCreepwave = analysisResult.notTankedCreepwaves.map { case (time, _, lane, players) => NotTankedCreepwave(players.map(heroId), lane, time) }
    val summonGoldFed = analysisResult.goldFedWithSummons.map { case (playerId, gold) => SummonGoldFed(heroId(playerId), gold) }.toSeq
    val midasEfficiency = analysisResult.midasEfficiency.map { case (playerId, efficiency) => MidasEfficiency(heroId(playerId), efficiency) }.toSeq
    val observersOnVision = analysisResult.obsPlacedOnVision.map { case (time, playerId) => ObserverOnVision(heroId(playerId), time) }
    val smokesOnVision = analysisResult.smokesUsedOnVision.map { case (time, playerId) => SmokeOnVision(heroId(playerId), time) }
    val outnumberedFights = analysisResult.badFights.map(fight => OutnumberedFight(fight.outnumberedTeam.get, fight.start))
    val badSmokeFights = analysisResult.smokeOnVisionButWonFight.map { case (fightStart, smokeTime, smokedTeam) => BadSmokeFight(smokedTeam, smokeTime, fightStart) }
    val worthlessGlyphs = analysisResult.glyphOnDeadT2.flatMap { case (team, glyphs) => glyphs.map(glyph => WorthlessGlyph(team, glyph)) }.toSeq

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
      outnumberedFights,
      badSmokeFights,
      worthlessGlyphs
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
