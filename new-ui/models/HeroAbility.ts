import { HeroId } from "./HeroId";

export interface HeroAbility {
  id: number
  slot: number
  displayName: string
  isGrantedByShard: boolean
  isGrantedByScepter: boolean
}

export interface HeroAbilities {
  heroId: HeroId
  abilities: HeroAbility[]
}