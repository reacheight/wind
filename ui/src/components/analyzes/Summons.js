import React from 'react';
import styles from '../../styles/Analysis.module.css'

const Summons = (props) => {
  const summonInfo = Object.keys(props.summons).map((id) =>
    <li key={'summon' + id.toString()}>
      <span className={styles.gold}>{props.summons[id]}</span> by <span className={styles.glowing}>{props.heroes[id]}</span>
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