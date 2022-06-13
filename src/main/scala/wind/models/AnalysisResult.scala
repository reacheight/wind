package wind.models

import wind.models.Lane.Lane
import wind.models.Team.Team

case class HeroId(id: Int)

case class UnusedItem(user: HeroId, target: HeroId, item: String, time: GameTimeState, withBlink: Boolean = false)
case class UnusedAbility(user: HeroId, target: HeroId, ability: String, time: GameTimeState, withBlink: Boolean = false)
case class OverlappedStun(user: HeroId, target: HeroId, time: GameTimeState)
case class CourierState(owner: HeroId, isOut: Boolean, isVersusMK: Boolean)
case class NotTankedCreepwave(heroes: Seq[HeroId], lane: Lane, time: GameTimeState)
case class SummonGoldFed(owner: HeroId, gold: Int)
case class MidasEfficiency(hero: HeroId, efficiency: Float)
case class ObserverOnVision(hero: HeroId, time: GameTimeState, destroyed: Boolean)
case class SmokeOnVision(hero: HeroId, time: GameTimeState)
case class BadFightJsonModel(outnumberedTeam: Team, seenHeroes: Seq[HeroId], time: GameTimeState)
case class BadSmokeFight(smokedTeam: Team, smokeTime: GameTimeState, fightTime: GameTimeState)
case class LostFightsUnderTheSameWard(loser: Team, fights: Seq[GameTimeState], wardOwner: HeroId)
case class FightLostUnderEnemyVision(loser: Team, time: GameTimeState)
case class UnreasonableDive(loser: Team, time: GameTimeState)
case class WorthlessGlyph(team: Team, times: Seq[GameTimeState])
case class MouseClickItemDelivery(heroId: HeroId, time: GameTimeState)

case class Analysis(
  unusedItems: Seq[UnusedItem],
  unusedAbilities: Seq[UnusedAbility],
  overlappedStuns: Seq[OverlappedStun],
  couriersState: Seq[CourierState],
  notTankedCreepwaves: Seq[NotTankedCreepwave],
  summonGoldFed: Seq[SummonGoldFed],
  midasEfficiency: Seq[MidasEfficiency],
  observersOnVision: Seq[ObserverOnVision],
  smokesOnVision: Seq[SmokeOnVision],
  badFights: Seq[BadFightJsonModel],
  badSmokeFights: Seq[BadSmokeFight],
  worthlessGlyphs: Seq[WorthlessGlyph],
  lostFightsUnderTheSameWard: Seq[LostFightsUnderTheSameWard],
  fightsLostUnderEnemyVision: Seq[FightLostUnderEnemyVision],
  unreasonableDives: Seq[UnreasonableDive],
  mouseClickItemDeliveries: Seq[MouseClickItemDelivery],
)


case class MatchInfo(radiant: Seq[HeroId], dire: Seq[HeroId], radiantWon: Boolean, matchLength: Int)
case class AnalysisResult(matchInfo: MatchInfo, analysis: Analysis)
