import { HeroId } from "./HeroId";

export interface HeroAbility {
  id: number
  slot: number
  displayName: number
  isGrantedByShard: boolean
  isGrantedByScepter: boolean
}

export interface HeroAbilities {
  heroId: HeroId
  abilities: HeroAbility[]
}