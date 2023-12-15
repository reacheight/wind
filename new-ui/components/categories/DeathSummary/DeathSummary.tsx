import { DeathSummary } from "../../../models/DeathSummary";
import { HeroId } from "../../../models/HeroId";
import { VStack } from "@chakra-ui/react";
import DeathSummaryShort from "./DeathSummaryShort";
import styles from "./DeathSummary.module.css";

interface DeathSummaryProps {
  target: HeroId
  deathSummary: ReadonlyArray<DeathSummary>
}

const DeathSummary = ({ target, deathSummary }: DeathSummaryProps) => {
  const targetDeathSummary = deathSummary.filter(s => s.hero === target)
  if (targetDeathSummary.length === 0)
    return null

  return (
    <div>
      <span className={styles.title}>Death Summary</span>
      <VStack align={'left'}>
        {targetDeathSummary.map(entry => <DeathSummaryShort deathSummaryEntry={entry} />)}
      </VStack>
    </div>
  )
}

export default DeathSummary