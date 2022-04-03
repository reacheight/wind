import React from 'react';
import styles from '../Analysis/Analysis.module.css'
import '../items.css'
import { getItemClassName } from '../util';

const UnusedItems = (props) => {
  const unusedItemsInfo = props.unusedItems.map(entry =>
    <li key={'unused' + entry[2] + entry[0]}>
      <span className={styles.glowing}>{props.heroes[entry[1]]}</span> <span className={styles.gray}>died</span> without using <span className={getItemClassName(entry[2])}>{entry[2]}</span> at {entry[0]}
    </li>
  )

  const unusedOnAllyItemsInfo = props.unusedOnAllyItems.map(([time, deadId, allyId, item]) => {
    let deadHeroName = <span className={styles.glowing}>{props.heroes[deadId]}</span>
    let allyHeroName = <span className={styles.glowing}>{props.heroes[allyId]}</span>
    let itemName = <span className={getItemClassName(item)}>{item}</span>
    return <li key={'unusedOnAllyItem' + deadHeroName + allyHeroName + item}>
      {allyHeroName} didn't use {itemName} on {deadHeroName} at {time}
    </li>
  })

  return (
    <>
      <h5 className={styles.analysisTitle}>Unused items ⚰️</h5>
      <ul>{unusedItemsInfo}</ul>
      <ul>{unusedOnAllyItemsInfo}</ul>
    </>
  )
}

export default UnusedItems