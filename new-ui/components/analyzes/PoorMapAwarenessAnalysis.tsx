import {BadSmokeFight} from "../../models/BadSmokeFight";
import styles from "../../styles/CommonAnalysis.module.css";
import { Stack } from "@chakra-ui/layout";
import { Collapse, useDisclosure } from "@chakra-ui/react";
import {getOppositeTeam, Team} from "../../models/Team";

interface PoorMapAwarenessProps {
  badSmokeFights: ReadonlyArray<BadSmokeFight>
}

const PoorMapAwarenessAnalysis = ({ badSmokeFights }: PoorMapAwarenessProps) => {
  const { isOpen, onToggle } = useDisclosure({ defaultIsOpen: true})

  const entries = badSmokeFights.map(fight => {
    let smokeTime = <span className={styles.name}>{fight.smokeTime}</span>
    let fightTime = <span className={styles.time}>{fight.fightTime}</span>
    let smokedTeam = <span className={styles.name}>{Team[fight.smokedTeam]}</span>
    let badFightTeam = <span className={styles.name}>{Team[getOppositeTeam(fight.smokedTeam)]}</span>

    return <div className={styles.entry}>
      {fightTime}
      {badFightTeam} saw {smokedTeam} use smoke at {smokeTime}, but didn't react and lost fight anyway
    </div>
  })

  return (
    <div className={styles.container}>
      <button className={styles.title} onClick={onToggle}>Poor map awareness</button>
      <Collapse in={isOpen}>
        <div className={styles.entries}>
          <Stack>
            {entries}
          </Stack>
        </div>
      </Collapse>
    </div>
  )
}

export default PoorMapAwarenessAnalysis