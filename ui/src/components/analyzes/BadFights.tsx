import styles from "../../styles/Analysis.module.css";
import '../../items.css'
import React from "react";
import { List, ListItem } from "@chakra-ui/react";
import {BadFight} from "../../models/BadFight";
import { getOppositeTeam, Team } from "../../models/Team";
import { Heroes } from "../../constants/heroes";
import { BadSmokeFight } from "../../models/BadSmokeFight";
import { LostFightsUnderTheSameWard } from "../../models/LostFightsUnderTheSameWard";
import { UnreasonableDive } from "../../models/UnreasonableDive";

type BadFightsProps = {
  badFights: ReadonlyArray<BadFight>;
  badSmokeFights: ReadonlyArray<BadSmokeFight>;
  lostFightsUnderTheSameWard: ReadonlyArray<LostFightsUnderTheSameWard>;
  unreasonableDives: ReadonlyArray<UnreasonableDive>;
}

const BadFights = ({ badFights, badSmokeFights, lostFightsUnderTheSameWard, unreasonableDives }: BadFightsProps) => {
  const list = badFights.map(({ outnumberedTeam, seenHeroes, time }) => {
    let teamName = <span className={styles[Team[outnumberedTeam]]}>{Team[outnumberedTeam]}</span>
    let winnerTeam = getOppositeTeam(outnumberedTeam)
    let winnerTeamName = <span className={styles[Team[winnerTeam]]}>{Team[winnerTeam]}</span>
    let seenHeroesNames = <span className={styles.glowing}>{seenHeroes.map(id => Heroes[id]).join(", ")}</span>
    return <>
      <ListItem key={time}>
        {time} — {seenHeroesNames} from {teamName} showed up far on enemy vision, that allowed {winnerTeamName} to outnumber the rest of their opponents in the fight.{" "}
        {seenHeroesNames} should have come to fight or {teamName} should have retreated.
      </ListItem>
      <br/>
    </>
  })

  const badSmokeFightsList = badSmokeFights.map(({ smokedTeam, smokeTime, fightTime }) => {
    let smokedTeamName = <span className={styles[Team[smokedTeam]]}>{Team[smokedTeam]}</span>
    let oppositeTeam = getOppositeTeam(smokedTeam)
    let oppositeTeamName = <span className={styles[Team[oppositeTeam]]}>{Team[oppositeTeam]}</span>
    return <li key={fightTime}>
      {oppositeTeamName} saw {smokedTeamName} use smoke at <span className={styles.glowing}>{smokeTime}</span>, but didn't react and lost fight anyway at <span className={styles.glowing}>{fightTime}</span>
    </li>
  })

  const lostFightsUnderWardList = lostFightsUnderTheSameWard.map(({ loser, fights, wardOwner }) => {
    let loserTeamName = <span className={styles[Team[loser]]}>{Team[loser]}</span>
    let fightsStarts = <span className={styles.glowing}>{fights.join(", ")}</span>
    let wardOwnerHeroName = <span className={styles.glowing}>{Heroes[wardOwner]}</span>

    return <li key={fights.join(", ")}>
      {loserTeamName} lost {fights.length} fights at {fightsStarts} under the same <span className={"observer"}>Observer</span> by {wardOwnerHeroName}
    </li>
  })

  const dives = unreasonableDives.map(({ loser, time }) => {
    let loserTeamName = <span className={styles[Team[loser]]}>{Team[loser]}</span>
    let diveTime = <span className={styles.glowing}>{time}</span>
    return <li key={time}>
      {loserTeamName} dived too far at {diveTime}
    </li>
  })

  return (
    <>
      <h5 className={styles.analysisTitle}>Bad fights</h5>
      <List>{list}</List>
      <List>{badSmokeFightsList}</List>
      {badSmokeFightsList.length !== 0 && <br/>}
      <List>{lostFightsUnderWardList}</List>
      {lostFightsUnderWardList.length !== 0 && <br/>}
      <List>{dives}</List>
    </>
  )
}

export default BadFights