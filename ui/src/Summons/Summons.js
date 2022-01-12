import React from 'react';
import styles from '../Analysis/Analysis.module.css'

const Summons = (props) => {
  const summonInfo = Object.keys(props.summons).map((id) =>
    <li key={'summon' + id.toString()}>
      {props.summons[id]} by <span className={styles.heroName}>{props.heroes[id]}</span>
    </li>
  )

  return (
    <>
      <h5 className={styles.analysisTitle}>Gold fed with summons 🪙</h5>
      <ul>{summonInfo}</ul>
    </>
  )
}

export default Summons