import { getUserContext } from "./UserContextWrapper";
import { useEffect, useState } from "react";
import Match from "../models/Match";
import Routes from "../api/routs";
import { List, ListItem } from "@chakra-ui/layout";
import MatchPreview from "./MatchPreview";
import styles from '../styles/Matches.module.css'
import { useRouter } from "next/router";
import { Button } from "@chakra-ui/button";

const Matches = () => {
  let userContext = getUserContext()

  if (userContext.loading || userContext.user === null)
    return null

  const router = useRouter()
  const { page } = router.query
  const truePage = page ? +page : 1

  const onNextClick = () => {
    router.query.page = truePage + 1
    router.push(router)
  }

  const onPrevClick = () => {
    router.query.page = truePage - 1
    router.push(router)
  }

  let [matches, setMatches] = useState<ReadonlyArray<Match>>(new Array<Match>())

  useEffect(() => {
    fetch(Routes.Players.getMatches(userContext.user.id, truePage))
      .then(response => response.json())
      .then(json => setMatches(json))
  }, [page])

  if (matches.length === 0)
    return null

  return (
    <div className={styles.container}>
      <span className={styles.title}>Matches</span>
      <List>
        {matches.map(match => <ListItem id={match.id}><MatchPreview match={match} /></ListItem>)}
      </List>
      <div>
        {(truePage !== 1) && <Button onClick={() => onPrevClick()}>{"<"} Prev</Button>}
        <Button onClick={() => onNextClick()}>Next {">"}</Button>
      </div>
    </div>
  )
}

export default Matches