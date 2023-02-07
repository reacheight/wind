// Generated by ScalaTS 0.5.10: https://scala-ts.github.io/scala-ts/

import { HeroId } from './HeroId';
import { AbilityId } from "./AbilityId";

export interface OverlappedStun {
  user: HeroId;
  target: HeroId;
  time: string;
  overlappedTime: number;
  sourceId: number;
  isAbility: boolean;
}