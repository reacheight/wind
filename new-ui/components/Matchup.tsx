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

  let radiantTeam = radiantHeroes
  let direTeam = direHeroes

  const radiantWinner = <div className={styles.radiantWinner}>Winner</div>
  const direWinner = <div className={styles.direWinner}>Winner</div>
  const minutes = Math.floor(match.durationSeconds / 60)
  const seconds = ('0' + (match.durationSeconds % 60)).slice(-2)

  return (
    <div>
      <div className={styles.sides}>
        <div className={styles.sidesElement}>
          <div className={styles.side}>
            <div className={styles.radiant}>
              <Image src={"/radiant.png"} width={60} height={60} />
            </div>
            {match.didRadiantWin && radiantWinner}
          </div>
        </div>
        <div className={styles.sidesElement}>
          <div className={styles.duration}>
            <span>{minutes}:{seconds}</span>
          </div>
        </div>
        <div className={styles.sidesElement}>
          <div className={styles.side}>
            {!match.didRadiantWin && direWinner}
            <div className={styles.dire}>
              <Image src={"/dire.png"} width={60} height={60} />
            </div>
          </div>
        </div>
      </div>
      <div className={styles.matchup}>
        <div className={styles.team}>
          {radiantTeam}
        </div>
        <span className={styles.vs}>vs</span>
        <div className={styles.team}>
          {direTeam}
        </div>
      </div>
    </div>
  )
}

export default Matchup