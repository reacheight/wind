import React from 'react';
import styles from '../../styles/Analysis.module.css'
import { SummonGoldFed } from "../../models/SummonGoldFed";
import { Heroes } from "../../constants/heroes";

type SummonGoldFedProps = {
  summonGoldFed: ReadonlyArray<SummonGoldFed>;
}

const Summons = ({ summonGoldFed }: SummonGoldFedProps) => {
  const summonGoldFedList = summonGoldFed.map(entry =>
    <li key={entry.owner}>
      <span className={styles.gold}>{entry.gold}</span> by <span className={styles.glowing}>{Heroes[entry.owner]}</span>
    </li>
  )

  return (
    <>
      <h5 className={styles.analysisTitle}>Gold fed with summons 🪙</h5>
      <ul>{summonGoldFedList}</ul>
    </>
  )
}

export default Summons