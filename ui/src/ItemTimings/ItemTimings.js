import React, { useEffect, useState } from 'react';
import { DotaItems, DotaHeroes, formatName, isEmpty, formatTime } from '../util';
import styles from '../Analysis/Analysis.module.css'

const ItemTimings = (props) => {
  const heroId = DotaHeroes.find(h => h.name === props.hero).id
  const [heroTimingsData, setTimings] = useState({})

  useEffect(() => {
    fetch(`https://stats.spectral.gg/lrg2/api/?league=imm_ranked_731&mod=items/icritical-heroid${heroId}&item_cat=medium,major`)
      .then(response => response.json())
      .then(data => setTimings(data.result.items))
  }, [heroId])

  if (isEmpty(heroTimingsData)) {
    return <></>
  }

  const playerPurchases = props.purchases.map(purchase => {
    let item = purchase[0]
    let time = purchase[1]

    return [DotaItems.find(i => i.name === item).id.toString(), time]
  }).filter(entry => Object.keys(heroTimingsData).includes(entry[0]))

  const getTimingGrade = (playerTiming, averageTimings) => {
    if (playerTiming <= averageTimings.q1) {
      return <span className={styles.green}>Early ({"<="} {formatTime(averageTimings.q1)})</span>
    }

    if (playerTiming <= averageTimings.median) {
      return <span className={styles.lightgreen}>Average ({"<="} {formatTime(averageTimings.median)})</span>
    }

    if (playerTiming < averageTimings.critical_time) {
      return <span className={styles.yellow}>Later than average ({">"} {formatTime(averageTimings.median)})</span>
    }
    
    return <span className={styles.red}>Late ({">="} {formatTime(averageTimings.critical_time)})</span>
  }

  const itemInfo = playerPurchases.map(purchase => {
    let itemId = purchase[0]
    let playerTiming = purchase[1]
    let averageTimings = heroTimingsData[itemId]
    let itemName = formatName(DotaItems.find(i => i.id.toString() === itemId).name)

    return <li key={"timings" + "hero" + itemId}>
      {itemName} - {formatTime(playerTiming)} - {getTimingGrade(playerTiming, averageTimings)}
    </li>
  })

  return (
    <div>
      <h5 className={styles.analysisTitle}><span className={styles.heroName}>{formatName(props.hero)}</span></h5>
      <ul>
        {itemInfo}
      </ul>
    </div>
  )
}

export default ItemTimings