import {BadSmokeFight} from "../../models/BadSmokeFight";
import styles from "../../styles/CommonAnalysis.module.css";
import {Stack} from "@chakra-ui/layout";
import {Collapse, useDisclosure} from "@chakra-ui/react";
import {getOppositeTeam, Team} from "../../models/Team";

interface PoorMapAwarenessProps {
  badSmokeFights: ReadonlyArray<BadSmokeFight>
}

const PoorMapAwarenessAnalysis = ({ badSmokeFights }: PoorMapAwarenessProps) => {
  const { isOpen, onToggle } = useDisclosure({ defaultIsOpen: true})

  const entries = badSmokeFights.map(fight => {
    let smokeTime = <span className={styles.time}>{fight.smokeTime}</span>
    let badFightTeam = getOppositeTeam(fight.smokedTeam)
    let smokedTeamName = <span className={fight.smokedTeam == Team.Radiant ? styles.radiant : styles.dire}>{Team[fight.smokedTeam]}</span>
    let badFightTeamName = <span className={badFightTeam == Team.Radiant ? styles.radiant : styles.dire}>{Team[badFightTeam]}</span>

    return <div className={styles.entry}>
      {smokeTime}
      <span>{badFightTeamName} saw {smokedTeamName} use smoke, but didn't react and lost fight anyway</span>
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