// Generated by ScalaTS 0.5.10: https://scala-ts.github.io/scala-ts/

import { HeroId } from './HeroId';

export interface MatchInfo {
  radiant: ReadonlyArray<HeroId>;
  dire: ReadonlyArray<HeroId>;
  radiantWon: boolean;
  matchLength: number;
}