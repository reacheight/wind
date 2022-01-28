import React from 'react';
import styles from '../Analysis/Analysis.module.css'

const UnusedAbilities = (props) => {
  const unusedAbilitiesInfo = props.unusedAbilities.map(entry =>
    <li key={'unused' + entry[2] + entry[0]}>
      <span className={styles.heroName}>{props.heroes[entry[1]]}</span> <span className={styles.gray}>died</span> without using <span className={styles.heroName}>{entry[2]}</span> at {entry[0]}
    </li>
  )

  const unusedOnAllyAbilitiesInfo = props.unusedOnAllyAbilities.map(entry =>
    <li key={'unusedOnAlly' + entry[3] + entry[0]}>
      <span className={styles.heroName}>{props.heroes[entry[2]]}</span> didn't use <span className={styles.heroName}>{entry[3]}</span> on <span className={styles.heroName}>{props.heroes[entry[1]]}</span> at {entry[0]}
    </li>
  )

  return (
    <>
      <h5 className={styles.analysisTitle}>Unused abilities ☠️</h5>
      <ul>{unusedAbilitiesInfo}</ul>
      <ul>{unusedOnAllyAbilitiesInfo}</ul>
    </>
  )
}

export default UnusedAbilities