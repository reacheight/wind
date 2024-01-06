import { DamageReceived } from "../../../models/DamageReceived";
import { PlayerHero } from "../../../models/PlayerHero";
import { DamageAmount } from "../../../models/DamageAmount";
import styles from "./DeathSummary.module.css";
import { calculateFullDamageReceived } from "../../../utils";
import { PlayerId } from "../../../models/PlayerId";
import { Tooltip } from "@chakra-ui/tooltip";

interface DamageTypeLineProps {
  damageReceivedList: ReadonlyArray<DamageReceived>
  playersHeroes: ReadonlyArray<PlayerHero>
}

interface DamageTypeEntry {
  type: string,
  amount: number,
  color: string
}

const getDamageTypeColor = (type: string) => {
  switch (type) {
    case "pure": return "#d98d04"
    case "magical": return "#4d7bb9"
    case "physical": return "#ad1a1a"
  }
}

const calculateDamageOfType = (damageAmountList: ReadonlyArray<DamageAmount>, type: string) => damageAmountList.map(d => d[type]).reduce((a, b) => a + b, 0)
const calculateFullDamageOfType = (damageReceivedList: ReadonlyArray<DamageReceived>, type: string) =>
  damageReceivedList.map(d => d.attackDamage[type]
    + calculateDamageOfType(d.abilityDamage.map(ad => ad.damage), type)
    + calculateDamageOfType(d.itemDamage.map(id => id.damage), type)).reduce((a, b) => a + b, 0)

const DamageTypeLine = ({ damageReceivedList, playersHeroes }: DamageTypeLineProps) => {
  const totalDamage = damageReceivedList.map(dr => calculateFullDamageReceived(dr)).reduce((a, b) => a + b, 0)
  const subLines = Array("pure", "magical", "physical")
    .map(type => ({ type, amount: calculateFullDamageOfType(damageReceivedList, type), color: getDamageTypeColor(type) }) as DamageTypeEntry)
    .map(entry => {
      let percentage = (entry.amount / totalDamage) * 100
      return (
        <Tooltip label={entry.amount + ' ' + entry.type + ' damage'} bg='black' color={'darkgrey'} borderRadius={'6px'}>
          <div className={styles.damageSubLine} style={{width: percentage + '%', backgroundColor: entry.color}}></div>
        </Tooltip>
      )
    })

  return (
    <div className={styles.damageSummaryLine}>
      <div className={styles.damageTypeFullLine}>
        {subLines}
      </div>
    </div>
  )
}

export default DamageTypeLine