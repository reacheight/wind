import {Lane} from "../../util";
import styles from "../../styles/Analysis.module.css";
import React from "react";
import AnalysisHint from "../AnalysisHint";

const NotTankedCreepwaves = (props) => {
  const notTankedCreepwavesInfo = props.notTankedCreepwaves.map(([time, team, lane, players]) => {
    let heroes = <span className={styles.glowing}>{players.map(id => props.heroes[id]).join(", ")}</span>
    return <li key={"notTankedCreepwave" + time + heroes}>
      {heroes} <span className={styles.gray}>didn't tank</span> creepwave at {time} in {Lane(lane)} lane
    </li>
  })

  let hintContent = <span>
    Tanking lane creeps for a few seconds to not let them go under you tower allows you to keep the lane near your T1 and farm safely.
  </span>
  return (
    <>
      <h5 className={styles.analysisTitle}>Not tanked creepwaves 🛡️</h5>
      <AnalysisHint hint={hintContent} />
      <ul>{notTankedCreepwavesInfo}</ul>
    </>
  )
}

export default NotTankedCreepwaves