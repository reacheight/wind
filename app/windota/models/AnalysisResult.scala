package windota.models

import windota.models.Lane.Lane
import windota.models.Team.Team
import windota.models.dto._

case class HeroId(id: Int)
case class ItemId(id: Int)
case class AbilityId(id: Int)

case class OverlappedStun(user: HeroId, target: HeroId, time: GameTimeState, overlappedTime: Float, sourceId: Int, isAbility: Boolean)
case class CourierState(owner: HeroId, isOut: Boolean, isVersusMK: Boolean)
case class NotTankedCreepwave(heroes: Seq[HeroId], lane: Lane, time: GameTimeState)
case class SummonGoldFed(owner: HeroId, gold: Int)
case class MidasEfficiency(hero: HeroId, efficiency: Float)
case class ObserverOnVision(hero: HeroId, time: GameTimeState, destroyed: Boolean)
case class SmokeOnVision(hero: HeroId, time: GameTimeState)
case class BadFightJsonModel(outnumberedTeam: Team, seenHeroes: Seq[SeenHero], time: GameTimeState)
case class SeenHero(heroId: HeroId, location: Location)
case class BadSmokeFight(smokedTeam: Team, smokeTime: GameTimeState, fightTime: GameTimeState)
case class LostFightsUnderTheSameWard(loser: Team, fights: Seq[GameTimeState], wardOwner: HeroId)
case class FightLostUnderEnemyVision(loser: Team, time: GameTimeState)
case class UnreasonableTeamDive(loser: Team, time: GameTimeState)
case class UnreasonableHeroDive(hero: HeroId, time: GameTimeState, towerTier: Int)
case class WorthlessGlyph(team: Team, times: Seq[GameTimeState])
case class MouseClickItemDelivery(heroId: HeroId, count: Int)
case class MouseClickQuickBuy(heroId: HeroId, count: Int)
case class CampBlock(blocker: HeroId, time: GameTimeState)
case class NotUnblockedCamp(heroId: HeroId, blocks: Seq[CampBlock])
case class NotPurchasedStick(heroId: HeroId, stickHeroId: HeroId)
case class NotPurchasedItemAgainstHero(heroId: HeroId, itemName: String, noItemWinrate: Int, itemWinrate: Int, candidates: Seq[HeroId])
case class PowerTreadsAbilityUsages(heroId: HeroId, total: Int, onInt: Int, manaLost: Float)
case class UnreactedLaneGank(target: HeroId, gankers: Seq[HeroId], time: GameTimeState, lane: Lane)

case class Analysis(
  unusedItems: Seq[UnusedItem],                        // HUD
  unusedAbilities: Seq[UnusedAbility],                 // HUD
  overlappedStuns: Seq[OverlappedStun],                // HUD
  midasEfficiency: Seq[MidasEfficiency],               // HUD
  couriersState: Seq[CourierState],                             // lane
  notTankedCreepwaves: Seq[NotTankedCreepwave],                 // lane
  notUnblockedCamps: Seq[NotUnblockedCamp],                     // lane
  observersOnVision: Seq[ObserverOnVision],              // vision (надо сделать типа ты видел что враги поставили вард, но не сломал)
  smokesOnVision: Seq[SmokeOnVision],                    // vision (не чекнул перед проюзом смока вражеский вижен)
  badFights: Seq[BadFightJsonModel],                                   // bad fights panel
  badSmokeFights: Seq[BadSmokeFight],                                  // bad fights panel
  lostFightsUnderTheSameWard: Seq[LostFightsUnderTheSameWard],         // bad fights panel
  unreasonableTeamDives: Seq[UnreasonableTeamDive],                    // bad fights panel
//  unreactedLaneGanks: Seq[UnreactedLaneGank],                          // ? bad fights panel ?
  unreasonableHeroDive: Seq[UnreasonableHeroDive],
  summonGoldFed: Seq[SummonGoldFed],
  worthlessGlyphs: Seq[WorthlessGlyph],
  fightsLostUnderEnemyVision: Seq[FightLostUnderEnemyVision],
  mouseClickItemDeliveries: Seq[MouseClickItemDelivery],         // mouse instead of hotkeys
  mouseClickQuickBuys: Seq[MouseClickQuickBuy],                  // mouse instead of hotkeys
  notPurchasedSticks: Seq[NotPurchasedStick],                          // build
  notPurchasedItemAgainstHero: Seq[NotPurchasedItemAgainstHero],       // build
  powerTreadsAbilityUsages: Seq[PowerTreadsAbilityUsages],                   // HUD
  unreasonableHeroDives: Seq[UnreasonableHeroDive],                         // self category
  // todo: пачки крипов, которые умерли об тавер         //self category

  scepterOwners: Seq[HeroId],
  shardOwners: Seq[HeroId],
  deathSummary: Seq[DeathSummary],
)


case class MatchInfo(radiant: Seq[HeroId], dire: Seq[HeroId], radiantWon: Boolean, matchLength: Int)
case class AnalysisResult(matchInfo: MatchInfo, analysis: Analysis)
