import { DamageReceived } from "../../../models/DamageReceived";
import { calculateFullDamageReceived } from "../../../utils";
import { HeroId } from "../../../models/HeroId";
import { PlayerHero } from "../../../models/PlayerHero";
import styles from "./DeathSummary.module.css";
import { PlayerId } from "../../../models/PlayerId";
import MiniIcon from "../../MiniIcon";
import { Tooltip } from '@chakra-ui/react'
import { Heroes } from "../../../constants/heroes";

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
    case 0: return "#1b2ab7"
    case 2: return "#11c7a7"
    case 4: return "#9709ad"
    case 6: return "#c9b005"
    case 8: return "#be4419"

    case 10: return "#cc47c4"
    case 12: return "#7fb20d"
    case 14: return "#11aed7"
    case 16: return "#00861d"
    case 18: return "#6c4101"
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
    let showIcon = percentage > 10
    let iconDisplayValue = showIcon ? 'inline' : 'none';
    let icon = <div style={{display: iconDisplayValue}}><MiniIcon heroId={hero} width={25} height={25} /></div>
    return (
      <Tooltip label={damage + ' damage from ' + Heroes[hero]} bg='black' color={'darkgrey'} borderRadius={'6px'}>
        <div className={styles.heroIconSubLine} style={{width: percentage + '%'}}>{icon}</div>
      </Tooltip>
    )
  })

  return (
    <div className={styles.damageSummaryLine}>
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