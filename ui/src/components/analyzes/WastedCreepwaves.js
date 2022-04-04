import styles from '../../styles/Analysis.module.css'
import React from "react";
import {Lane, Team} from "../../util";

const WastedCreepwaves = (props) => {
  const creepwavesInfo = props.wastedCreepwaves.map(([time, team, lane, tier]) => {
    let teamName = <span className={styles["team" + team]}>{Team(team)}</span>
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