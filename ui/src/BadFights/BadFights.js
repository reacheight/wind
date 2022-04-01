import styles from "../Analysis/Analysis.module.css";
import {Team} from "../util";
import React from "react";
import { List, ListItem } from "@chakra-ui/react";
import AnalysisHint from "../AnalysisHint/AnalysisHint";

const BadFights = ({ badFights }) => {
  const badFightsInfo = badFights.map(([team, start]) => {
    let teamName = <span className={styles["team" + team]}>{Team(team)}</span>
    return <ListItem key={"badFight" + team + start}>
      {teamName} were outnumbered at <span className={styles.glowing}>{start}</span>
    </ListItem>
  })

  const hintContent = <span>These fights were lost because some team members showed up far from the teammates, that allowed their opponents to get unequally better fight conditions</span>
  return (
    <>
      <h5 className={styles.analysisTitle}>Outnumbered fights</h5>
      <AnalysisHint hint={hintContent} />
      <List>{badFightsInfo}</List>
    </>
  )
}

export default BadFights