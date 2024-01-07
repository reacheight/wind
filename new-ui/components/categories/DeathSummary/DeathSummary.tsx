import { DeathSummary } from "../../../models/DeathSummary";
import { HeroId } from "../../../models/HeroId";
import { VStack } from "@chakra-ui/react";
import styles from "./DeathSummary.module.css";
import { PlayerHero } from "../../../models/PlayerHero";
import DeathSummaryFull from "./DeathSummaryFull";
import { HeroAbilities } from "../../../models/HeroAbility";
import { Item } from "../../../models/Item";

interface DeathSummaryProps {
  target: HeroId
  playersHeroes: ReadonlyArray<PlayerHero>
  deathSummary: ReadonlyArray<DeathSummary>
  abilities: HeroAbilities[]
  items: Item[]
}

const DeathSummary = ({ target, playersHeroes, deathSummary, abilities, items }: DeathSummaryProps) => {
  const targetDeathSummary = deathSummary.filter(s => s.hero === target)
  if (targetDeathSummary.length === 0)
    return null

  return (
    <div>
      <span className={styles.title}>Death Summary</span>
      <div className={styles.summaryList}>
        <VStack align={'left'}>
          {targetDeathSummary.map(entry => <DeathSummaryFull deathSummaryEntry={entry} playersHeroes={playersHeroes} abilities={abilities} items={items} />)}
        </VStack>
      </div>
    </div>
  )
}

export default DeathSummary