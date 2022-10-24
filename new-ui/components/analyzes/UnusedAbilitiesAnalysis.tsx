import { UnusedAbility } from "../../models/UnusedAbility";
import styles from "../../styles/CommonAnalysis.module.css";
import { Stack } from "@chakra-ui/layout";
import MiniIcon from "../MiniIcon";
import { Collapse, useDisclosure } from "@chakra-ui/react";

interface UnusedAbilitiesAnalysisProps {
  unusedAbilities: ReadonlyArray<UnusedAbility>
}

const UnusedAbilitiesAnalysis = ({ unusedAbilities }: UnusedAbilitiesAnalysisProps) => {
  const { isOpen, onToggle } = useDisclosure({ defaultIsOpen: true})

  const entries = unusedAbilities
    .map(entry => {
      let time = <span className={styles.time}>{entry.time}</span>
      let ability = <span className={styles.name}>{entry.ability}</span>
      let userIcon = <MiniIcon heroId={entry.user} />
      let targetIcon = <MiniIcon heroId={entry.target} />
      let isOnAlly = entry.user != entry.target
      let withBlinkPart = <><span className={styles.name}>Blink Dagger</span> and</>
      let onTargetPart = <>on {targetIcon}</>

      return <div className={styles.entry}>{time} {userIcon} didn't use {entry.withBlink && withBlinkPart} {ability} {isOnAlly && onTargetPart}</div>
    })

  return (
    <div className={styles.container}>
      <button className={styles.title} onClick={onToggle}>Unused abilities</button>
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

export default UnusedAbilitiesAnalysis