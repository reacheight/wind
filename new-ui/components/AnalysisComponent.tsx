import { Analysis } from "../models/Analysis";
import { HeroId } from "../models/HeroId";
import { Team } from "../models/Team";
import Laning from "./categories/Laning/Laning";

import styles from "../styles/AnalysisComponent.module.css"
import Hud from "./categories/Hud/Hud";
import { VStack } from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { HeroAbilities, HeroAbility } from "../models/HeroAbility";
import Routes from "../api/routs";
import { Item } from "../models/Item";

interface AnalysisComponentProps {
  heroes: ReadonlyArray<HeroId>
  targetHero: HeroId
  targetTeam: Team
  analysis: Analysis
}

const AnalysisComponent = ( { heroes, targetHero, targetTeam, analysis }: AnalysisComponentProps) => {
  const [abilities, setAbilities] = useState<HeroAbilities[]>(null)
  const [items, setItems] = useState<Item[]>(null)

  useEffect(() => {
    const itemsIds = analysis.unusedItems.map(i => i.item)
    if (analysis.powerTreadsAbilityUsages.length !== 0)
      itemsIds.push(63);

    fetch(Routes.Constants.getItems([...new Set(itemsIds)]))
      .then(response => response.json())
      .then(items => setItems(items))
  }, [])

  useEffect(() =>  {
    Promise.all(heroes.map(id =>
      fetch(Routes.Constants.getHeroAbilities(id))
        .then(response => response.json())
        .then(json => json as HeroAbility[])
        .then(abilities => ({ heroId: id, abilities } as HeroAbilities))
    ))
      .then(allAbilities => setAbilities(allAbilities.flat()))
  }, [])

  if (abilities === null)
    return null

  return (
    <div className={styles.analysis}>
      <VStack align={'left'} spacing={'30px'}>
        <Hud
          target={targetHero}
          unusedItems={analysis.unusedItems}
          unusedAbilities={analysis.unusedAbilities}
          overlappedStuns={analysis.overlappedStuns}
          midasEfficiency={analysis.midasEfficiency}
          powerTreadsAbilityUsages={analysis.powerTreadsAbilityUsages}
          shardOwners={analysis.shardOwners}
          scepterOwners={analysis.scepterOwners}
          allAbilities={abilities}
          allItems={items}
        />
      <Laning target={targetHero} couriersState={analysis.couriersState} notTankedCreepwaves={analysis.notTankedCreepwaves} notUnblockedCamps={analysis.notUnblockedCamps} />
      </VStack>
    </div>
  )
}

export default AnalysisComponent