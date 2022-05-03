import styles from "../../styles/Analysis.module.css";
import React from "react";
import { List, ListItem } from "@chakra-ui/react";
import AnalysisHint from "../AnalysisHint";
import {OutnumberedFight} from "../../models/OutnumberedFight";
import { Team } from "../../models/Team";

type BadFightsProps = {
  outnumberedFights: ReadonlyArray<OutnumberedFight>;
}

const BadFights = ({ outnumberedFights }: BadFightsProps) => {
  const badFights = outnumberedFights.map(({ outnumberedTeam, time }) => {
    let teamName = <span className={styles[Team[outnumberedTeam]]}>{Team[outnumberedTeam]}</span>
    return <ListItem key={time}>
      {teamName} were outnumbered at <span className={styles.glowing}>{time}</span>
    </ListItem>
  })

  const hintContent =
    "These fights were lost because some team members showed up far from the teammates, that allowed their opponents to get unequally better fight conditions."
  return (
    <>
      <h5 className={styles.analysisTitle}>Outnumbered fights</h5>
      <AnalysisHint hint={hintContent} />
      <List>{badFights}</List>
    </>
  )
}

export default BadFights