import { OverlappedStun } from "../../models/OverlappedStun";
import styles from "../../styles/CommonAnalysis.module.css"
import MiniIcon from "../MiniIcon";
import { Stack } from "@chakra-ui/layout";
import { Collapse, useDisclosure } from "@chakra-ui/react";

interface OverlappedStunsProps {
  overlappedStuns: ReadonlyArray<OverlappedStun>
}

const OverlappedStunsAnalysis = ({ overlappedStuns }: OverlappedStunsProps) => {
  const { isOpen, onToggle } = useDisclosure({ defaultIsOpen: true})

  const entries = overlappedStuns.map(entry => {
    let time = <span className={styles.time}>{entry.time}</span>
    let userIcon = <MiniIcon heroId={entry.user} />
    let targetIcon = <MiniIcon heroId={entry.target} />

    return <div className={styles.entry}>{time} {userIcon} stunned {targetIcon} too early</div>
  })

  return (
    <div className={styles.container}>
      <button className={styles.title} onClick={onToggle}>Overlapped stuns</button>
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

export default OverlappedStunsAnalysis