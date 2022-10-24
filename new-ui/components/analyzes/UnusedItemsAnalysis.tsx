import { UnusedItem } from "../../models/UnusedItem";
import styles from "../../styles/CommonAnalysis.module.css"
import { Stack } from "@chakra-ui/layout";
import MiniIcon from "../MiniIcon";
import { Collapse, useDisclosure } from "@chakra-ui/react";

interface UnusedItemsAnalysisProps {
  unusedItems: ReadonlyArray<UnusedItem>;
}

const UnusedItemsAnalysis = ({ unusedItems }: UnusedItemsAnalysisProps) => {
  const { isOpen, onToggle } = useDisclosure({ defaultIsOpen: true})

  const entries = unusedItems
    .map(entry => {
      let time = <span className={styles.time}>{entry.time}</span>
      let item = <span className={styles.name}>{entry.item}</span>
      let userIcon = <MiniIcon heroId={entry.user} />
      let targetIcon = <MiniIcon heroId={entry.target} />
      let isOnAlly = entry.user != entry.target
      let onTargetPart = <>on {targetIcon}</>

      return <div className={styles.entry}>{time} {userIcon} didn't use {item} {isOnAlly && onTargetPart}</div>
    })

  return (
    <div className={styles.container}>
      <button className={styles.title} onClick={onToggle}>Unused items before death</button>
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

export default UnusedItemsAnalysis