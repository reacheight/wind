import { OverlappedStun } from "../../models/OverlappedStun";
import styles from "../../styles/CommonAnalysis.module.css"
import MiniIcon from "../MiniIcon";
import { Stack } from "@chakra-ui/layout";

interface OverlappedStunsProps {
  overlappedStuns: ReadonlyArray<OverlappedStun>
}

const OverlappedStunsAnalysis = ({ overlappedStuns }: OverlappedStunsProps) => {
  const entries = overlappedStuns.map(entry => {
    let time = <span className={styles.time}>{entry.time}</span>
    let userIcon = <MiniIcon heroId={entry.user} />
    let targetIcon = <MiniIcon heroId={entry.target} />

    return <div className={styles.entry}>{time} {userIcon} stunned {targetIcon} too early</div>
  })

  return (
    <div className={styles.container}>
      <div className={styles.title}>Overlapped stuns</div>
      <div className={styles.entries}>
        <Stack>
          {entries}
        </Stack>
      </div>
    </div>
  )
}

export default OverlappedStunsAnalysis