// Generated by ScalaTS 0.5.10: https://scala-ts.github.io/scala-ts/

import { HeroId} from './HeroId';
import { Team} from './Team';

export interface LostFightsUnderTheSameWard {
  loser: Team;
  fights: ReadonlyArray<string>;
  wardOwner: HeroId;
}
