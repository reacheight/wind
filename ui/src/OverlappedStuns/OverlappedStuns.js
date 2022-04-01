import styles from '../Analysis/Analysis.module.css'
import {ListItem, List} from "@chakra-ui/react";
import React from "react";
import AnalysisHint from "../AnalysisHint/AnalysisHint";

const OverlappedStuns = ({ overlappedStuns, heroes }) => {
  const overlappedStunsInfo = overlappedStuns.map(([time, stunnedId, attackerId]) => {
    let stunnedHeroName = <span className={styles.glowing}>{heroes[stunnedId]}</span>
    let attackerHeroName = <span className={styles.glowing}>{heroes[attackerId]}</span>
    return <ListItem key={"overlappedStun" + time + stunnedId + attackerId}>
      {attackerHeroName} stunned {stunnedHeroName} too early at {time}
    </ListItem>
  })

  let hintContent = <snap>
    These stuns were cast too early on an already stunned target. Avoid overlapping stuns to maximize disable time.
  </snap>
  return (
    <>
      <h5 className={styles.analysisTitle}>Overlapped stuns (✨Experimental✨)</h5>
      <AnalysisHint hint={hintContent} />
      <List>{overlappedStunsInfo}</List>
    </>
  )
}

export default OverlappedStuns