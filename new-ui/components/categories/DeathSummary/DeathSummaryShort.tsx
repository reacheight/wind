import { DeathSummary } from "../../../models/DeathSummary";
import styles from "./DeathSummary.module.css"
import DamageReceivedLine from "./DamageReceivedLine";
import { PlayerHero } from "../../../models/PlayerHero";
import DamageTypeLine from "./DamageTypeLine";
import GoldPenalty from "./GoldPenalty";
import RespawnTime from "./RespawnTime";

interface DeathSummaryShortProps {
  deathSummaryEntry: DeathSummary
  playersHeroes: ReadonlyArray<PlayerHero>
}

const DeathSummaryShort = ({ deathSummaryEntry, playersHeroes }: DeathSummaryShortProps) => (
  <div className={styles.shortContainer}>
    <span className={styles.time}>{deathSummaryEntry.time}</span>
    <DamageReceivedLine damageReceivedList={deathSummaryEntry.damageReceived} playersHeroes={playersHeroes}/>
    <DamageTypeLine damageReceivedList={deathSummaryEntry.damageReceived} playersHeroes={playersHeroes}/>
    <GoldPenalty amount={deathSummaryEntry.goldPenalty}/>
    <RespawnTime time={deathSummaryEntry.respawnTime}/>
  </div>
)

export default DeathSummaryShort