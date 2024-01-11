import { PlayerId } from "./PlayerId";
import { Lane } from "./Lane";

export interface UnreactedLaneGank {
  target: PlayerId;
  gankers: ReadonlyArray<PlayerId>;
  gankTime: string;
  deathTime: string;
  lane: Lane;
}