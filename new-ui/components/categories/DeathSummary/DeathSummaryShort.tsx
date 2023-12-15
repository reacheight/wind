import { DeathSummary } from "../../../models/DeathSummary";
import styles from "./DeathSummary.module.css"
import Image from "next/image";
import { HStack } from "@chakra-ui/layout";

interface DeathSummaryShortProps {
  deathSummaryEntry: DeathSummary
}

const DeathSummaryShort = ({ deathSummaryEntry }: DeathSummaryShortProps) => {
  return (
    <div className={styles.shortContainer}>
      <HStack spacing={16}>
        <span className={styles.time}>{deathSummaryEntry.time}</span>
        <span className={styles.goldPenalty}>
          <div className={styles.goldIcon}><Image src={'/gold.webp'} width={20} height={20} /></div>
          -{deathSummaryEntry.goldPenalty}
        </span>
      </HStack>
    </div>
  )
}

export default DeathSummaryShort