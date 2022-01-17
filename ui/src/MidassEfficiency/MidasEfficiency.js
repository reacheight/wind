import styles from '../Analysis/Analysis.module.css'
import React from "react";

const MidasEfficiency = (props) => {
  const efficiencyInfo = Object.keys(props.midasEfficiency).map(id =>
    <li key={"midas" + id}>
      <span className={styles.heroName}>{props.heroes[id]}</span> — {props.midasEfficiency[id].toFixed(2)}
    </li>
  )

  return (
    <>
      <h5 className={styles.analysisTitle}>Midas efficiency</h5>
      <ul>{efficiencyInfo}</ul>
    </>
  )
}

export default MidasEfficiency