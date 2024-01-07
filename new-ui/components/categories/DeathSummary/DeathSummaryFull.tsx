import { DeathSummary } from "../../../models/DeathSummary";
import { PlayerHero } from "../../../models/PlayerHero";
import DeathSummaryShort from "./DeathSummaryShort";
import { useEffect, useState } from "react";
import DeathSummaryExpanded from "./DeathSummaryExpanded";
import { HeroAbilities } from "../../../models/HeroAbility";
import { Item } from "../../../models/Item";

interface DeathSummaryFullProps {
  deathSummaryEntry: DeathSummary
  playersHeroes: ReadonlyArray<PlayerHero>
  abilities: HeroAbilities[]
  items: Item[]
}

const DeathSummaryFull = ({ deathSummaryEntry, playersHeroes, abilities, items }: DeathSummaryFullProps) => {
  const [showExpanded, setShowExpanded] = useState(false)
  useEffect(() => setShowExpanded(false), [deathSummaryEntry])

  return (
    <div>
      <div onClick={() => setShowExpanded(!showExpanded)}>
        <DeathSummaryShort deathSummaryEntry={deathSummaryEntry} playersHeroes={playersHeroes} />
      </div>
      {showExpanded && <DeathSummaryExpanded deathSummaryEntry={deathSummaryEntry} abilities={abilities} items={items} />}
    </div>
  )
}

export default DeathSummaryFull