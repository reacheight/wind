import Match from "../models/Match";
import styles from '../styles/MatchPreview.module.css'
import { getUserContext } from "./UserContextWrapper";
import { Heroes } from "../constants/heroes";
import Link from "next/link";
import Image from "next/image";
import Routes from "../api/routs";
import Kda from "./Kda";

interface MatchPreviewProps {
  match: Match
}

const MatchPreview = ({ match }: MatchPreviewProps) => {
  let userContext = getUserContext()
  if (userContext.loading || userContext.user === null)
    return null

  let userPlayer = match.players.find(p => p.steamAccountId === userContext.user.id)
  let userHero = userPlayer.heroId
  let isVictory = userPlayer.isRadiant && match.didRadiantWin || !userPlayer.isRadiant && !match.didRadiantWin

  return (
    <Link href={'/matches/' + match.id}>
      <a>
        <div className={styles.preview}>
          <div className={styles.portraitAndHero}>
            <div className={styles.portrait}>
              <Image src={Routes.Images.getHorizontalPortrait(userHero)} layout={'fill'} objectFit={'contain'}/>
            </div>
            <div className={isVictory ? styles.heroWin : styles.heroLose}>{Heroes[userPlayer.heroId]}</div>
          </div>
          <div className={styles.kda}>
            <Kda player={userPlayer}/>
          </div>
        </div>
      </a>
    </Link>
  )
}

export default MatchPreview