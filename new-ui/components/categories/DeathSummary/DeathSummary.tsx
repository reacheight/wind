import { DeathSummary } from "../../../models/DeathSummary";
import { HeroId } from "../../../models/HeroId";
import { VStack } from "@chakra-ui/react";
import styles from "./DeathSummary.module.css";
import { PlayerHero } from "../../../models/PlayerHero";
import DeathSummaryFull from "./DeathSummaryFull";
import { HeroAbilities } from "../../../models/HeroAbility";
import { Item } from "../../../models/Item";
import { UnusedItem } from "../../../models/UnusedItem";
import { UnusedAbility } from "../../../models/UnusedAbility";

interface DeathSummaryProps {
  target: HeroId
  playersHeroes: ReadonlyArray<PlayerHero>
  deathSummary: ReadonlyArray<DeathSummary>
  abilities: HeroAbilities[]
  items: Item[]
  unusedItems: ReadonlyArray<UnusedItem>
  unusedAbilities: ReadonlyArray<UnusedAbility>
}

const DeathSummary = ({ target, playersHeroes, deathSummary, abilities, items, unusedItems, unusedAbilities }: DeathSummaryProps) => {
  const targetDeathSummary = deathSummary.filter(s => s.hero === target)
  if (targetDeathSummary.length === 0)
    return null

  return (
    <div>
      <span className={styles.title}>Death Summary</span>
      <div className={styles.summaryList}>
        <VStack align={'left'}>
          {targetDeathSummary.map(entry => <DeathSummaryFull deathSummaryEntry={entry} playersHeroes={playersHeroes} abilities={abilities} items={items} unusedItems={unusedItems} unusedAbilities={unusedAbilities} />)}
        </VStack>
      </div>
    </div>
  )
}

export default DeathSummary