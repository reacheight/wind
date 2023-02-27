import { NotTankedCreepwave } from "../../../models/NotTankedCreepwave";
import styles from './Laning.module.css'
import Image from "next/image";

interface NotTankedCreepwavesProps {
  notTankedCreepwaves: ReadonlyArray<NotTankedCreepwave>
}

const NotTankedCreepwaves = ({ notTankedCreepwaves }: NotTankedCreepwavesProps) => {
  const count = notTankedCreepwaves.length
  const times = notTankedCreepwaves.map(entry =>
    <span className={styles.white}>{entry.time}</span>
  ).reduce((prev, curr) => [prev, ', ', curr])

  return (
    <div className={styles.container}>
      <div className={styles.sentry}><Image src={'/tower.png'} width={80} height={120} /></div>
      <span className={styles.text}>
        You didn't <span className={styles.insight}>tank lane creeps</span> before your tower {count} time{count > 1 ? 's' : ''} at&nbsp;{times}
      </span>
    </div>
  )
}

export default NotTankedCreepwaves