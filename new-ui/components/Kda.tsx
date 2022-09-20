import MatchPlayer from "../models/MatchPlayer";
import styles from '../styles/Kda.module.css'

interface KdaProps {
  player: MatchPlayer
}

const Kda = ({ player }: KdaProps) => {
  const slash = <span className={styles.slash}>/</span>

  return (
    <div className={styles.kda}>
      {player.kills} {slash} {player.deaths} {slash} {player.deaths}
    </div>
  )
}

export default Kda