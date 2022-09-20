import Match from "../models/Match";
import styles from '../styles/Matchup.module.css'
import HeroMatchPreview from "./HeroMatchPreview";
import { HStack} from "@chakra-ui/layout";

interface MatchupProps {
  match: Match
}

const Matchup = ({ match }: MatchupProps) => {
  let radiantPlayers = match.players.filter(p => p.isRadiant)
  let direPlayers = match.players.filter(p => !p.isRadiant)

  let radiantHeroes = radiantPlayers.map(p => <HeroMatchPreview player={p}/>)
  let direHeroes = direPlayers.map(p => <HeroMatchPreview player={p}/>)

  let radiantTeam = <HStack spacing={4}>{radiantHeroes}</HStack>
  let direTeam = <HStack spacing={4}>{direHeroes}</HStack>

  return (
    <div className={styles.main}>
      {radiantTeam}
      <span className={styles.vs}>vs</span>
      {direTeam}
    </div>
  )
}

export default Matchup