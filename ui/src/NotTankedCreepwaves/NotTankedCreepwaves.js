import {Lane} from "../util";
import styles from "../Analysis/Analysis.module.css";
import React from "react";

const NotTankedCreepwaves = (props) => {
  const notTankedCreepwavesInfo = props.notTankedCreepwaves.map(([time, team, lane, players]) => {
    let heroes = <span className={styles.glowing}>{players.map(id => props.heroes[id]).join(", ")}</span>
    return <li key={"notTankedCreepwave" + time + heroes}>
      {heroes} <span className={styles.gray}>didn't tank</span> creepwave at {time} in {Lane(lane)} lane
    </li>
  })

  return (
    <>
      <h5 className={styles.analysisTitle}>Not tanked creepwaves 🛡️</h5>
      <ul>{notTankedCreepwavesInfo}</ul>
    </>
  )
}

export default NotTankedCreepwaves