import { SmokeOnVision } from "../../models/SmokeOnVision";
import styles from "../../styles/CommonAnalysis.module.css";
import MiniIcon from "../MiniIcon";
import { useDisclosure } from "@chakra-ui/hooks";
import { Collapse } from "@chakra-ui/transition";
import { Stack } from "@chakra-ui/layout";

interface SmokesOnVisionProps {
  smokesOnVision: ReadonlyArray<SmokeOnVision>;
}

const SmokesOnVisionAnalysis = ({ smokesOnVision }: SmokesOnVisionProps) => {
  const { isOpen, onToggle } = useDisclosure({ defaultIsOpen: true})

  const entries = smokesOnVision.map(entry => {
    let time = <span className={styles.time}>{entry.time}</span>
    let heroIcon = <MiniIcon heroId={entry.hero} />

    return <div className={styles.entry}>{time} {heroIcon}</div>
  })

  return (
    <div className={styles.container}>
      <button className={styles.title} onClick={onToggle}>Smokes used on enemy vision</button>
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

export default SmokesOnVisionAnalysis