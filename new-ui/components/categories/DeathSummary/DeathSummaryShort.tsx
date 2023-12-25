import { DeathSummary } from "../../../models/DeathSummary";
import styles from "./DeathSummary.module.css"
import Image from "next/image";
import DamageReceivedLine from "./DamageReceivedLine";
import { calculateFullDamageReceived } from "../../../utils";
import { PlayerHero } from "../../../models/PlayerHero";
import DamageTypeLine from "./DamageTypeLine";

interface DeathSummaryShortProps {
  deathSummaryEntry: DeathSummary
  playersHeroes: ReadonlyArray<PlayerHero>
}

const DeathSummaryShort = ({ deathSummaryEntry, playersHeroes }: DeathSummaryShortProps) => {
  return (
    <div className={styles.shortContainer}>
      <span className={styles.time}>{deathSummaryEntry.time}</span>
      <DamageReceivedLine damageReceivedList={deathSummaryEntry.damageReceived} playersHeroes={playersHeroes} />
      <DamageTypeLine damageReceivedList={deathSummaryEntry.damageReceived} playersHeroes={playersHeroes} />
      <span className={styles.goldPenalty}>
        <div className={styles.goldIcon}><Image src={'/gold.png'} width={20} height={20} /></div>
        -{deathSummaryEntry.goldPenalty}
      </span>
    </div>
  )
}

export default DeathSummaryShort