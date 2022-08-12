import MatchPlayer from "./MatchPlayer";

export default interface Match {
  id: number
  durationSeconds: number
  didRadiantWin: boolean
  players: ReadonlyArray<MatchPlayer>
}