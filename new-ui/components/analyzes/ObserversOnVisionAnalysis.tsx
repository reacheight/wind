import { ObserverOnVision } from "../../models/ObserverOnVision";
import styles from "../../styles/CommonAnalysis.module.css";
import MiniIcon from "../MiniIcon";
import { useDisclosure } from "@chakra-ui/hooks";
import { Collapse } from "@chakra-ui/transition";
import { Stack } from "@chakra-ui/layout";

interface ObserversOnVisionProps {
  observersOnVision: ReadonlyArray<ObserverOnVision>;
}

const ObserversOnVisionAnalysis = ({ observersOnVision }: ObserversOnVisionProps) => {
  const { isOpen, onToggle } = useDisclosure({ defaultIsOpen: true})

  const entries = observersOnVision.map(entry => {
    let time = <span className={styles.time}>{entry.time}</span>
    let heroIcon = <MiniIcon heroId={entry.hero} />

    return <div className={styles.entry}>{time} {heroIcon}</div>
  })

  return (
    <div className={styles.container}>
      <button className={styles.title} onClick={onToggle}>Observers placed on enemy vision</button>
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

export default ObserversOnVisionAnalysis