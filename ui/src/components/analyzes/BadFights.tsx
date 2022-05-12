import styles from "../../styles/Analysis.module.css";
import React from "react";
import { List, ListItem } from "@chakra-ui/react";
import {BadFight} from "../../models/BadFight";
import { getOppositeTeam, Team } from "../../models/Team";
import { Heroes } from "../../constants/heroes";
import { BadSmokeFight } from "../../models/BadSmokeFight";

type BadFightsProps = {
  badFights: ReadonlyArray<BadFight>;
  badSmokeFights: ReadonlyArray<BadSmokeFight>
}

const BadFights = ({ badFights, badSmokeFights }: BadFightsProps) => {
  const list = badFights.map(({ outnumberedTeam, seenHeroes, time }) => {
    let teamName = <span className={styles[Team[outnumberedTeam]]}>{Team[outnumberedTeam]}</span>
    let winnerTeam = getOppositeTeam(outnumberedTeam)
    let winnerTeamName = <span className={styles[Team[winnerTeam]]}>{Team[winnerTeam]}</span>
    let seenHeroesNames = <span className={styles.glowing}>{seenHeroes.map(id => Heroes[id])}</span>
    return <ListItem key={time}>
      {time} — {seenHeroesNames} from {teamName} showed up far on enemy vision, that allowed {winnerTeamName} to outnumber the rest of their opponents in the fight.{" "}
      {seenHeroesNames} should have come to fight or {teamName} should have retreated.
    </ListItem>
  })

  const badSmokeFightsList = badSmokeFights.map(({ smokedTeam, smokeTime, fightTime }) => {
    let smokedTeamName = <span className={styles[Team[smokedTeam]]}>{Team[smokedTeam]}</span>
    let oppositeTeam = getOppositeTeam(smokedTeam)
    let oppositeTeamName = <span className={styles[Team[oppositeTeam]]}>{Team[oppositeTeam]}</span>
    return <li key={fightTime}>
      {oppositeTeamName} saw {smokedTeamName} use smoke at <span className={styles.glowing}>{smokeTime}</span>, but didn't react and lost fight anyway at <span className={styles.glowing}>{fightTime}</span>
    </li>
  })

  return (
    <>
      <h5 className={styles.analysisTitle}>Bad fights</h5>
      <List>
        {list}
        {badSmokeFightsList}
      </List>
    </>
  )
}

export default BadFights