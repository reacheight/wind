import { NotUnblockedCamp } from "../../models/NotUnblockedCamp";
import styles from "../../styles/Analysis.module.css";
import React from "react";
import { Team } from "../../models/Team";
import { Lane } from "../../models/Lane";

type NotUnblockedCampsProps = {
  notUnblockedCamps: ReadonlyArray<NotUnblockedCamp>;
}

const NotUnblockedCamps = ({ notUnblockedCamps} : NotUnblockedCampsProps) => {
  let camps = notUnblockedCamps.map(({ team, lane, blocks }) => {
    let blockTimes = <span className={styles.glowing}>{blocks.join(", ")}</span>
    let teamName = <span className={styles[Team[team]]}>{Team[team]}</span>
    let laneName = <span className={styles.glowing}>{Lane[lane]}</span>
    return <li key={team.toString() + lane.toString()}>
      {teamName} didn't unblock their {laneName} lane camp, blocked at {blockTimes}
    </li>
  })

  return (
    <>
      <h5 className={styles.analysisTitle}>Not unblocked camps️</h5>
      <ul>{camps}</ul>
    </>
  )
}

export default NotUnblockedCamps