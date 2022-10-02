import { Lane } from './Lane';
import { Team } from './Team';

export interface NotUnblockedCamp {
  team: Team;
  lane: Lane;
  blocks: ReadonlyArray<string>;
}