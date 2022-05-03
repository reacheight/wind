import styles from "../../styles/Analysis.module.css";
import React from "react";
import { BadSmokeFight } from "../../models/BadSmokeFight";
import { getOppositeTeam, Team } from "../../models/Team";

type BadSmokeFightsProps = {
  badSmokeFights: ReadonlyArray<BadSmokeFight>;
}

const BadSmokeFights = ({ badSmokeFights }: BadSmokeFightsProps) => {
  if (badSmokeFights.length === 0)
    return <></>

  const badSmokeFightsInfo = badSmokeFights.map(({ smokedTeam, smokeTime, fightTime }) => {
    let smokedTeamName = <span className={styles[Team[smokedTeam]]}>{Team[smokedTeam]}</span>
    let oppositeTeam = getOppositeTeam(smokedTeam)
    let oppositeTeamName = <span className={styles[Team[oppositeTeam]]}>{Team[oppositeTeam]}</span>
    return <li key={fightTime}>
      {oppositeTeamName} saw {smokedTeamName} use smoke at <span className={styles.glowing}>{smokeTime}</span>, but didn't react and lost fight anyway at <span className={styles.glowing}>{fightTime}</span>
    </li>
  })

  return (
    <>
      <h5 className={styles.analysisTitle}>Poor map awareness 👁️</h5>
      <ul>{badSmokeFightsInfo}</ul>
    </>
  )
}

export default BadSmokeFights