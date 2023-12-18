import { DamageReceived } from "../../../models/DamageReceived";
import { calculateFullDamageReceived } from "../../../utils";
import { HeroId } from "../../../models/HeroId";
import { PlayerHero } from "../../../models/PlayerHero";
import styles from "./DeathSummary.module.css";
import { PlayerId } from "../../../models/PlayerId";
import MiniIcon from "../../MiniIcon";
import { percent } from "style-value-types";

interface DamageReceivedLineProps {
  damageReceivedList: ReadonlyArray<DamageReceived>
  playersHeroes: ReadonlyArray<PlayerHero>
}

interface DamageFromHero {
  hero: HeroId
  damage: number
}

const getPlayerColor = (playerId: PlayerId) => {
  switch (playerId) {
    case 0: return "#1D1D70FF"
    case 2: return "#31ad96"
    case 4: return "#61146e"
    case 6: return "#675b1c"
    case 8: return "#c74b1c"

    case 10: return "#b450b0"
    case 12: return "#66802e"
    case 14: return "#1f6d81"
    case 16: return "#1c6409"
    case 18: return "#543403"
  }
}

const DamageReceivedLine = ({ damageReceivedList, playersHeroes }: DamageReceivedLineProps) => {
  const damageAmountFromHeroes = damageReceivedList
    .map(dr => ({ hero: dr.from, damage: calculateFullDamageReceived(dr) } as DamageFromHero))
    .sort((a, b) => a.damage - b.damage)
  const fullDamage = damageAmountFromHeroes.map(dr => dr.damage).reduce((a, b) => a + b, 0)

  const colors = ['green', 'yellow', 'red', 'purple', 'blue']

  const subLines = damageAmountFromHeroes.map(({ hero, damage }) => {
    let playerId = playersHeroes.find(ph => ph.heroId === hero).playerId
    let percentage = (damage / fullDamage) * 100
    return <div className={styles.damageSubLine} style={{width: percentage + '%', backgroundColor: getPlayerColor(playerId)}}></div>
  })

  const heroIcons = damageAmountFromHeroes.map(({ hero, damage }) => {
    let percentage = (damage / fullDamage) * 100
    let showIcon = percentage > 8
    let iconDisplayValue = showIcon ? 'inline' : 'none';
    let icon = <div style={{display: iconDisplayValue}}><MiniIcon heroId={hero} width={20} height={20} /></div>
    return <div className={styles.heroIconSubLine} style={{width: percentage + '%'}}>{icon}</div>
  })

  return (
    <div>
      <div className={styles.damageFullLine}>
        {subLines}
      </div>
      <div className={styles.heroIconsLine}>
        {heroIcons}
      </div>
    </div>
  )
}

export default DamageReceivedLine