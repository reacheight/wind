import { HeroId } from "./HeroId";

export interface  CampBlock {
  blocker: HeroId;
  time: string;
}

export interface NotUnblockedCamp {
  heroId: HeroId;
  blocks: ReadonlyArray<CampBlock>;
}