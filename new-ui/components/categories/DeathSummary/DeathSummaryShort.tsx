import { DeathSummary } from "../../../models/DeathSummary";
import styles from "./DeathSummary.module.css"
import DamageReceivedLine from "./DamageReceivedLine";
import { PlayerHero } from "../../../models/PlayerHero";
import DamageTypeLine from "./DamageTypeLine";
import GoldPenalty from "./GoldPenalty";
import RespawnTime from "./RespawnTime";
import deathSummaryFull from "./DeathSummaryFull";

interface DeathSummaryShortProps {
  deathSummaryEntry: DeathSummary
  playersHeroes: ReadonlyArray<PlayerHero>
  isExpanded: boolean
}

const DeathSummaryShort = ({ deathSummaryEntry, playersHeroes, isExpanded }: DeathSummaryShortProps) => (
  <div className={styles.shortContainer} style={isExpanded ? {background: 'rgba(255, 255, 255, 0.15)'} : {}}>
    <span className={styles.time}>{deathSummaryEntry.time}</span>
    <DamageReceivedLine damageReceivedList={deathSummaryEntry.damageReceived} playersHeroes={playersHeroes}/>
    <DamageTypeLine damageReceivedList={deathSummaryEntry.damageReceived} playersHeroes={playersHeroes}/>
    <GoldPenalty amount={deathSummaryEntry.goldPenalty + deathSummaryEntry.goldEarnings.map(e => e.amount).reduce((a, b) => a + b, 0)}/>
    <RespawnTime time={deathSummaryEntry.respawnTime}/>
  </div>
)

export default DeathSummaryShort