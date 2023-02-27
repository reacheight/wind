import { NotUnblockedCamp } from "../../../models/NotUnblockedCamp";

import styles from './Laning.module.css'
import MiniIcon from "../../MiniIcon";
import Image from "next/image";

interface NotUnblockedCampsProps {
  notUnblockedCamp: NotUnblockedCamp
}

const NotUnblockedCamps = ({ notUnblockedCamp }: NotUnblockedCampsProps) => {
  const blocksText = notUnblockedCamp.blocks.map(block => {
    let blockerIcon = <MiniIcon heroId={block.blocker} />
    return <span>at <span className={styles.white}>{block.time}</span> by {blockerIcon}</span>
  }).reduce((prev, curr) => [prev, ', ', curr])

  return (
    <div className={styles.container}>
      <div className={styles.sentry}><Image src={"/sentry.png"} width={80} height={120} /></div>
      <span className={styles.text}>
        You didn't <span className={styles.insight}>unblock</span> your lane <span className={styles.insight}>neutral camp</span>, blocked {blocksText}
      </span>
    </div>
  )
}

export default NotUnblockedCamps