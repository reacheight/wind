import React, { useEffect, useState } from 'react';
import { items, heroes, formatName } from '../util';
import styles from '../Analysis/Analysis.module.css'

const ItemTimings = (props) => {
  const heroId = heroes.result.heroes.find(h => h.name === props.hero).id
  const [timings, setTimings] = useState({})

  useEffect(() => {
    fetch(`https://stats.spectral.gg/lrg2/api/?league=imm_ranked_meta_last_7&mod=items/icritical-heroid${heroId}`)
      .then(resp => resp.json())
      .then(data => setTimings(data))
  }, [heroId])

  if (Object.keys(timings).length === 0) {
    return <></>
  }

  const timingItems = Object.keys(timings.result.items)
  const heroItems = props.purchases.map(entry => {
    let item = entry[0]
    let time = entry[1]

    return [items.result.items.find(i => i.name === item).id.toString(), time]
  }).filter(entry => timingItems.includes(entry[0]))

  const getTimingGrade = (playerTiming, averageTimings) => {
    if (playerTiming <= averageTimings.q1) {
      return <span className={styles.green}>Early</span>
    }

    if (playerTiming < averageTimings.critical_time) {
      return <span className={styles.yellow}>Average</span>
    }
    
    return <span className={styles.red}>Late</span>
  }

  const itemInfo = heroItems.map(entry => {
    let itemId = entry[0]
    let playerTiming = entry[1]
    let heroTimings = timings.result.items[entry[0]]
    let itemName = formatName(items.result.items.find(i => i.id.toString() === itemId).name)

    return <li key={"timings" + "hero" + itemId}>
      {itemName} - {getTimingGrade(playerTiming, heroTimings)}
    </li>
  })

  return (
    <div>
      <h5 className={styles.analysisTitle}><span className={styles.heroName}>{formatName(props.hero)}</span> timings</h5>
      <ul>
        {itemInfo}
      </ul>
    </div>
  )
}

export default ItemTimings