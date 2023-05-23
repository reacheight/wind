import Match from "../models/Match";
import styles from '../styles/Matchup.module.css'
import HeroMatchPreview from "./HeroMatchPreview";
import { HStack} from "@chakra-ui/layout";
import Image from "next/image";

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

  const radiantWinner = <span className={styles.radiantWinner}>Winner</span>
  const direWinner = <span className={styles.direWinner}>Winner</span>

  return (
    <div>
      <div className={styles.sides}>
        <div className={styles.side}>
          <div className={styles.radiant}>
            <Image src={"/radiant.png"} width={60} height={60} />
          </div>
          {match.didRadiantWin && radiantWinner}
        </div>
        <div className={styles.side}>
          {!match.didRadiantWin && direWinner}
          <div className={styles.dire}>
            <Image src={"/dire.png"} width={60} height={60} />
          </div>
        </div>
      </div>
      <div className={styles.matchup}>
        {radiantTeam}
        <span className={styles.vs}>vs</span>
        {direTeam}
      </div>
    </div>
  )
}

export default Matchup