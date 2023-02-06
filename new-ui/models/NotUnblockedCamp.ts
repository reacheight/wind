import { HeroId } from "./HeroId";

export interface  CampBlock {
  blocker: HeroId;
  time: string;
}

export interface NotUnblockedCamp {
  responsible: HeroId;
  blocks: ReadonlyArray<CampBlock>;
}