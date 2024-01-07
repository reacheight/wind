import { DeathSummary } from "../../../models/DeathSummary";
import { PlayerHero } from "../../../models/PlayerHero";
import DeathSummaryShort from "./DeathSummaryShort";
import { useEffect, useState } from "react";
import DeathSummaryExpanded from "./DeathSummaryExpanded";
import { HeroAbilities } from "../../../models/HeroAbility";
import { Item } from "../../../models/Item";
import { UnusedItem } from "../../../models/UnusedItem";
import { UnusedAbility } from "../../../models/UnusedAbility";

interface DeathSummaryFullProps {
  deathSummaryEntry: DeathSummary
  playersHeroes: ReadonlyArray<PlayerHero>
  abilities: HeroAbilities[]
  items: Item[]
  unusedItems: ReadonlyArray<UnusedItem>
  unusedAbilities: ReadonlyArray<UnusedAbility>
}

const DeathSummaryFull = ({ deathSummaryEntry, playersHeroes, abilities, items, unusedItems, unusedAbilities }: DeathSummaryFullProps) => {
  const [showExpanded, setShowExpanded] = useState(false)
  useEffect(() => setShowExpanded(false), [deathSummaryEntry])

  return (
    <div>
      <div onClick={() => setShowExpanded(!showExpanded)}>
        <DeathSummaryShort deathSummaryEntry={deathSummaryEntry} playersHeroes={playersHeroes} />
      </div>
      {showExpanded && <DeathSummaryExpanded deathSummaryEntry={deathSummaryEntry} abilities={abilities} items={items} unusedItems={unusedItems} unusedAbilities={unusedAbilities} />}
    </div>
  )
}

export default DeathSummaryFull