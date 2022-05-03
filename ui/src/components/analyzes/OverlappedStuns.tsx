import styles from '../../styles/Analysis.module.css'
import { List, ListItem } from "@chakra-ui/react";
import React from "react";
import AnalysisHint from "../AnalysisHint";
import { OverlappedStun } from "../../models/OverlappedStun";
import { Heroes } from "../../constants/heroes";

type OverlappedStunsProps = {
  overlappedStuns: ReadonlyArray<OverlappedStun>;
}

const OverlappedStuns = ({ overlappedStuns }: OverlappedStunsProps) => {
  const stuns = overlappedStuns.map(stun => {
    let stunnedHeroName = <span className={styles.glowing}>{Heroes[stun.target]}</span>
    let attackerHeroName = <span className={styles.glowing}>{Heroes[stun.user]}</span>
    return <ListItem key={stun.time}>
      {attackerHeroName} stunned {stunnedHeroName} too early at {stun.time}
    </ListItem>
  })

  return (
    <>
      <h5 className={styles.analysisTitle}>Overlapped stuns (✨Experimental✨)</h5>
      <AnalysisHint hint={"These stuns were cast too early on an already stunned target. Avoid overlapping stuns to maximize disable time."} />
      <List>{stuns}</List>
    </>
  )
}

export default OverlappedStuns