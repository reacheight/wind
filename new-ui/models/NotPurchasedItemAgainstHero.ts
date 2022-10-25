import {HeroId} from "./HeroId";

export default interface NotPurchasedItemAgainstHero {
  heroId: HeroId
  itemName: string
  noItemWinrate: number
  itemWinrate: number
  candidates: ReadonlyArray<HeroId>
}