import React from 'react';
import styles from '../../styles/Analysis.module.css'
import { UnusedAbility } from "../../models/UnusedAbility";
import { Heroes } from "../../constants/heroes";

type UnusedAbilitiesProps = {
  unusedAbilities: ReadonlyArray<UnusedAbility>;
}

const UnusedAbilities = ({ unusedAbilities }: UnusedAbilitiesProps) => {
  const unusedAbilitiesList = unusedAbilities.map(({ user, target, ability, time, withBlink}) => {
    let targetName = <span className={styles.glowing}>{Heroes[target]}</span>
    let userName = <span className={styles.glowing}>{Heroes[user]}</span>
    let abilityName = <span className={styles.glowing}>{ability}</span>
    let blinkDagger = <span className={styles.glowing}>Blink Dagger</span>

    if (withBlink)
      return <li key={time}>
        {userName} didn't use {blinkDagger} and {abilityName} on {targetName} at {time}
      </li>

    if (user === target)
      return <li key={time}>
        {targetName} <span className={styles.gray}>died</span> without using {abilityName} at {time}
      </li>

    return <li key={time}>
      {userName} didn't use {abilityName} on {targetName} at {time}
    </li>
  })

  return (
    <>
      <h5 className={styles.analysisTitle}>Unused abilities ☠️</h5>
      <ul>{unusedAbilitiesList}</ul>
    </>
  )
}

export default UnusedAbilities