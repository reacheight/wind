import Match from "../models/Match";
import styles from '../styles/MatchPreview.module.css'
import { getUserContext } from "./UserContextWrapper";
import { Heroes } from "../constants/heroes";

interface MatchPreviewProps {
  match: Match
}

const MatchPreview = ({ match }: MatchPreviewProps) => {
  let userContext = getUserContext()
  if (userContext.loading || userContext.user === null)
    return null

  let userPlayer = match.players.find(p => p.steamAccountId === userContext.user.id)
  let isVictory = userPlayer.isRadiant && match.didRadiantWin || !userPlayer.isRadiant && !match.didRadiantWin

  return (
    <a href={'/matches/' + match.id}>
      <div className={isVictory ? styles.win : styles.lose}>
        <div className={styles.hero}>{Heroes[userPlayer.heroId]}</div>
      </div>
    </a>
  )
}

export default MatchPreview