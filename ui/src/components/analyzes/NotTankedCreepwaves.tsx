import styles from "../../styles/Analysis.module.css";
import React from "react";
import AnalysisHint from "../AnalysisHint";
import { NotTankedCreepwave } from "../../models/NotTankedCreepwave";
import { Heroes } from "../../constants/heroes";
import { getLaneName } from "../../models/Lane";

type NotTankedCreepwavesProps = {
  notTankedCreepwaves: ReadonlyArray<NotTankedCreepwave>;
}

const NotTankedCreepwaves = ({ notTankedCreepwaves }: NotTankedCreepwavesProps) => {
  const notTankedCreepwavesInfo = notTankedCreepwaves.map(({ heroes, lane, time }) => {
    let heroesNames = <span className={styles.glowing}>{heroes.map(id => Heroes[id]).join(", ")}</span>
    return <li key={time}>
      {heroesNames} <span className={styles.gray}>didn't tank</span> creepwave at {time} in {getLaneName(lane)} lane
    </li>
  })

  let hintContent =
    "Tanking lane creeps for a few seconds to not let them go under you tower allows you to keep the lane near your T1 and farm safely."
  return (
    <>
      <h5 className={styles.analysisTitle}>Not tanked creepwaves 🛡️</h5>
      <AnalysisHint hint={hintContent} />
      <ul>{notTankedCreepwavesInfo}</ul>
    </>
  )
}

export default NotTankedCreepwaves