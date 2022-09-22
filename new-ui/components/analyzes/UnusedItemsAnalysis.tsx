import { UnusedItem } from "../../models/UnusedItem";
import styles from "../../styles/CommonAnalysis.module.css"
import { Stack } from "@chakra-ui/layout";
import Image from "next/image";
import Routes from "../../api/routs";

interface UnusedItemsAnalysisProps {
  unusedItems: ReadonlyArray<UnusedItem>;
}

const UnusedItemsAnalysis = ({ unusedItems }: UnusedItemsAnalysisProps) => {
  const getIcon = (heroId: number) => <span className={styles.heroIcon}><Image src={Routes.Images.getIcon(heroId)} width={30} height={30} /></span>

  const entries = unusedItems
    .map(entry => {
      let time = <span className={styles.time}>{entry.time}</span>
      let item = <span className={styles.item}>{entry.item}</span>
      let userIcon = getIcon(entry.user)
      let targetIcon = getIcon(entry.target)
      let isOnAlly = entry.user != entry.target
      let onTargetPart = <>on {targetIcon}</>

      return <div className={styles.entry}>{time} {userIcon} didn't use {item} {isOnAlly && onTargetPart}</div>
    })

  return (
    <div className={styles.container}>
      <div className={styles.title}>Unused items</div>
      <div className={styles.entries}>
        <Stack>
          {entries}
        </Stack>
      </div>
    </div>
  )
}

export default UnusedItemsAnalysis