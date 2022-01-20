import styles from '../Analysis/Analysis.module.css'
import React from "react";
import {Lane, Team} from "../util";

const WastedCreepwaves = (props) => {
  const creepwavesInfo = props.wastedCreepwaves.map(entry => {
    let [time, team, lane, tier] = entry
    let teamName = <span className={team === "0" ? styles.green : styles.red}>{Team(team)}</span>
    return <li key={"creepwave" + team + lane + tier + time}>
      {teamName} {Lane(lane)} T{tier} tower at {time}
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