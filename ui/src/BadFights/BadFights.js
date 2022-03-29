import styles from "../Analysis/Analysis.module.css";
import {Team} from "../util";
import React from "react";
import {Box, List, ListItem} from "@chakra-ui/react";

const BadFights = ({ badFights }) => {
  const badFightsInfo = badFights.map(([team, start]) => {
    let teamName = <span className={styles["team" + team]}>{Team(team)}</span>
    return <ListItem key={"badFight" + team + start}>
      {teamName} were outnumbered at <span className={styles.glowing}>{start}</span>
    </ListItem>
  })

  return (
    <>
      <h5 className={styles.analysisTitle}>Outnumbered fights</h5>
      <Box border={"none"} backgroundColor={"dimgray"} textColor={"gray.100"} borderRadius={10} textAlign={"center"} marginInline={"10%"} marginBottom={3} padding={3}>
        These fights were lost because some team members showed up far from the teammates, that allowed their opponents to get unequally better fight conditions
      </Box>
      <List>{badFightsInfo}</List>
    </>
  )
}

export default BadFights