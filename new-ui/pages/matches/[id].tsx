import { useRouter } from "next/router";
import { getUserContext } from "../../components/UserContextWrapper";
import { useEffect, useState } from "react";
import Routes from "../../api/routs";
import Match from "../../models/Match";
import styles from "../../styles/Match.module.css"

const Match = () => {
  let userContext = getUserContext()
  if (userContext.loading || userContext.user === null)
    return null

  const router = useRouter()
  const { id: matchId } = router.query

  const [match, setMatch] = useState<Match>(null)

  useEffect(() => {
    fetch(Routes.Matches.get(matchId))
      .then(response => response.json())
      .then(json => setMatch(json))
  }, [])

  if (match == null)
    return null

  return (
    <div>
      <div className={styles.title}>{matchId}</div>
      <div className={styles.outcome}>
        <span className={match.didRadiantWin ? styles.radiant : styles.dire}>{match.didRadiantWin ? 'Radiant' : 'Dire'}</span> won
      </div>
    </div>
  )
}

export default Match