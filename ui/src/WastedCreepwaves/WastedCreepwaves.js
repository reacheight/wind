import styles from '../Analysis/Analysis.module.css'
import React from "react";

const WastedCreepwaves = (props) => {
  const creepwavesInfo = props.wastedCreepwaves.map(entry => {
    let [time, tower] = entry
    return <li key={"creepwave" + tower + time}>
      <span className={styles.heroName}>{tower}</span> tower at {time}
    </li>
  })

  return (
    <>
      <h5 className={styles.analysisTitle}>Creepwaves wasted to towers</h5>
      <ul>{creepwavesInfo}</ul>
    </>
  )
}

export default WastedCreepwaves