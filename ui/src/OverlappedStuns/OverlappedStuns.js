import styles from '../Analysis/Analysis.module.css'
import {ListItem, List} from "@chakra-ui/react";
import React from "react";

const OverlappedStuns = ({ overlappedStuns, heroes }) => {
  const overlappedStunsInfo = overlappedStuns.map(([time, stunnedId, attackerId]) => {
    let stunnedHeroName = <span className={styles.glowing}>{heroes[stunnedId]}</span>
    let attackerHeroName = <span className={styles.glowing}>{heroes[attackerId]}</span>
    return <ListItem key={"overlappedStun" + time + stunnedId + attackerId}>
      {attackerHeroName} stunned {stunnedHeroName} too early at {time}
    </ListItem>
  })

  return (
    <>
      <h5 className={styles.analysisTitle}>Overlapped stuns</h5>
      <List>{overlappedStunsInfo}</List>
    </>
  )
}

export default OverlappedStuns