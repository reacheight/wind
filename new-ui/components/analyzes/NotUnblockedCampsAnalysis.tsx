import {NotUnblockedCamp} from "../../models/NotUnblockedCamp";
import styles from "../../styles/CommonAnalysis.module.css"
import { useDisclosure } from "@chakra-ui/hooks";
import { Collapse } from "@chakra-ui/transition";
import { Stack } from "@chakra-ui/layout";
import {Team} from "../../models/Team";
import {getLaneName} from "../../models/Lane";

interface NotUnblockedCampsProps {
  notUnblockedCamps: ReadonlyArray<NotUnblockedCamp>
}

const NotUnblockedCampsAnalysis = ({ notUnblockedCamps }: NotUnblockedCampsProps) => {
  const { isOpen, onToggle } = useDisclosure({ defaultIsOpen: true})

  const entries = notUnblockedCamps.map(entry => {
    let time = <span className={styles.time}>{entry.blocks.join(', ')}</span>
    let team = <span className={entry.team == Team.Dire ? styles.dire : styles.radiant}>{Team[entry.team]}</span>
    let lane = <span className={styles.lane}>{getLaneName(entry.lane)}</span>

    return <div className={styles.entry}>{time} {team} in {lane} lane</div>
  })

  return (
    <div className={styles.container}>
      <button className={styles.title} onClick={onToggle}>Not unblocked camps</button>
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

export default NotUnblockedCampsAnalysis