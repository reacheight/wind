import styles from "../Analysis/Analysis.module.css";
import {Team} from "../util";
import React from "react";

const BadSmokeFights = ({ badSmokeFights }) => {
  if (badSmokeFights.length === 0)
    return <></>

  const badSmokeFightsInfo = badSmokeFights.map(([fightStart, smokeTime, smokedTeam]) => {
    let smokedTeamName = <span className={styles["team" + smokedTeam]}>{Team(smokedTeam)}</span>
    let oppositeTeam = (smokedTeam + 1) % 2
    let oppositeTeamName = <span className={styles["team" + oppositeTeam]}>{Team(oppositeTeam)}</span>
    return <li key={"badSmokeFight" + smokedTeam + fightStart}>
      {oppositeTeamName} saw {smokedTeamName} use smoke at <span className={styles.glowing}>{smokeTime}</span>, but didn't react and lost fight anyway at <span className={styles.glowing}>{fightStart}</span>
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