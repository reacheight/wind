import React from 'react';
import styles from '../Analysis/Analysis.module.css'

const PowerTreads = (props) => {
  const ptInfo = Object.keys(props.powerThreadsAbilityUsage).map((id) => {
    let usageCount = props.powerThreadsAbilityUsage[id]
    return <li key={'ptAbility' + id}>
      <span className={styles.glowing}>{props.heroes[id]}</span> used {usageCount[0]} spells total with PT, {usageCount[1]} on <span className={styles.intelligence}>Intelligence</span>
    </li>
  })

  const notOnStrengthInfo = props.ptNotOnStrength.map(entry =>
    <li key={'ptNotOnStrength' + entry[0]}>
      <span className={styles.glowing}>{props.heroes[entry[1]]}</span> <span className={styles.gray}>died</span> with PT not on <span className={styles.red}>Strength</span> at {entry[0]}
    </li>
  )

  return (
    <>
      <h5 className={styles.analysisTitle}>Switching Power Treads ❤️💙💚</h5>
      <ul>{ptInfo}</ul>
      <ul>{notOnStrengthInfo}</ul>
    </>
  )
}

export default PowerTreads