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
import Resources from "./categories/Resources/Resources";
import DeathSummary from "./categories/DeathSummary/DeathSummary";
import { MatchInfo } from "../models/MatchInfo";

interface AnalysisComponentProps {
  heroes: ReadonlyArray<HeroId>
  targetHero: HeroId
  targetTeam: Team
  analysis: Analysis
  matchInfo: MatchInfo
}

const AnalysisComponent = ( { heroes, targetHero, targetTeam, analysis, matchInfo }: AnalysisComponentProps) => {
  const [abilities, setAbilities] = useState<HeroAbilities[]>(null)
  const [items, setItems] = useState<Item[]>(null)

  useEffect(() => {
    const damageItemsIds = analysis.deathSummary.flatMap(s => s.damageReceived.flatMap(dr => dr.itemDamage.map(id => id.ItemId)))
    const unusedItemsIds = analysis.unusedItems.map(i => i.item)
    if (analysis.powerTreadsAbilityUsages.length !== 0)
      unusedItemsIds.push(63);

    fetch(Routes.Constants.getItems([...new Set(unusedItemsIds.concat(damageItemsIds))]))
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
        <DeathSummary target={targetHero} deathSummary={analysis.deathSummary} playersHeroes={matchInfo.playersHeroes} abilities={abilities} items={items} unusedItems={analysis.unusedItems} unusedAbilities={analysis.unusedAbilities} unreactedLaneGanks={analysis.unreactedLaneGanks} />
        <Laning target={targetHero} couriersState={analysis.couriersState} notTankedCreepwaves={analysis.notTankedCreepwaves} notUnblockedCamps={analysis.notUnblockedCamps} />
        <Resources target={targetHero} summonGoldFed={analysis.summonGoldFed} />
      </VStack>
    </div>
  )
}

export default AnalysisComponent