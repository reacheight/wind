import { getUserContext } from "./UserContextWrapper";
import { useEffect, useState } from "react";
import Match from "../models/Match";
import AccountRoutes from "../api/account";
import { List, ListItem } from "@chakra-ui/layout";
import MatchPreview from "./MatchPreview";
import styles from '../styles/MatchesList.module.css'

const MatchesList = () => {
  let userContext = getUserContext()

  if (userContext.loading || userContext.user === null)
    return null

  let [matches, setMatches] = useState<ReadonlyArray<Match>>(new Array<Match>())

  useEffect(() => {
    fetch(AccountRoutes.matches, { credentials: 'include'})
      .then(response => response.json())
      .then(json => setMatches(json))
  }, [])

  if (matches.length === 0)
    return null

  return (
    <div>
      <span className={styles.title}>Matches</span>
      <List>
        {matches.map(match => <ListItem id={match.id}><MatchPreview match={match} /></ListItem>)}
      </List>
    </div>
  )
}

export default MatchesList