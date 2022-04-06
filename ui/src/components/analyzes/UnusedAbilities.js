import React from 'react';
import styles from '../../styles/Analysis.module.css'
import {getItemClassName} from "../../util";

const UnusedAbilities = (props) => {
  const unusedAbilitiesInfo = props.unusedAbilities.map(entry =>
    <li key={'unused' + entry[2] + entry[0]}>
      <span className={styles.glowing}>{props.heroes[entry[1]]}</span> <span className={styles.gray}>died</span> without using <span className={styles.glowing}>{entry[2]}</span> at {entry[0]}
    </li>
  )

  const unusedOnAllyAbilitiesInfo = props.unusedOnAllyAbilities.map(entry =>
    <li key={'unusedOnAlly' + entry[3] + entry[0]}>
      <span className={styles.glowing}>{props.heroes[entry[2]]}</span> didn't use <span className={styles.glowing}>{entry[3]}</span> on <span className={styles.glowing}>{props.heroes[entry[1]]}</span> at {entry[0]}
    </li>
  )

  const unusedOnAllyWithBlinkAbilitiesInfo = props.unusedOnAllyWithBlinkAbilities.map(([time, deadId, allyId, ability]) => {
    let deadHeroName = <span className={styles.glowing}>{props.heroes[deadId]}</span>
    let allyHeroName = <span className={styles.glowing}>{props.heroes[allyId]}</span>
    let abilityName = <span className={styles.glowing}>{ability}</span>
    let blinkDagger = <span className={styles.glowing}>Blink Dagger</span>
    return <li key={'unusedOnAllyWithBlinkAbility' + deadId + allyId + ability}>
      {allyHeroName} didn't use {blinkDagger} and {abilityName} on {deadHeroName} at {time}
    </li>
  })

  return (
    <>
      <h5 className={styles.analysisTitle}>Unused abilities ☠️</h5>
      <ul>{unusedAbilitiesInfo}</ul>
      <ul>{unusedOnAllyAbilitiesInfo}</ul>
      <ul>{unusedOnAllyWithBlinkAbilitiesInfo}</ul>
    </>
  )
}

export default UnusedAbilities