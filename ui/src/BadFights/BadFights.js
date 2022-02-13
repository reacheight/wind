import styles from "../Analysis/Analysis.module.css";
import {Team} from "../util";
import React from "react";

const BadFights = ({ badFights }) => {
  const badFightsInfo = badFights.map(([team, start]) => {
    let teamName = <span className={styles["team" + team]}>{Team(team)}</span>
    return <li key={"badFight" + team + start}>
      {teamName} accepted a bad fight at {start}
    </li>
  })

  return (
    <>
      <h5 className={styles.analysisTitle}>Bad fights</h5>
      <ul>{badFightsInfo}</ul>
    </>
  )
}

export default BadFights