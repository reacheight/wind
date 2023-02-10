import { Analysis } from "../models/Analysis";
import { HeroId } from "../models/HeroId";
import { Team } from "../models/Team";
import Laning from "./categories/Laning/Laning";

import styles from "../styles/AnalysisComponent.module.css"
import Hud from "./categories/Hud/Hud";
import { VStack } from "@chakra-ui/react";

interface AnalysisComponentProps {
  targetHero: HeroId
  targetTeam: Team
  analysis: Analysis
}

const AnalysisComponent = ( { targetHero, targetTeam, analysis }: AnalysisComponentProps) => {

  return (
    <div className={styles.analysis}>
      <VStack align={'left'} spacing={'30px'}>
      <Laning target={targetHero} couriersState={analysis.couriersState} notTankedCreepwaves={analysis.notTankedCreepwaves} notUnblockedCamps={analysis.notUnblockedCamps} />
      <Hud
        target={targetHero}
        unusedItems={analysis.unusedItems}
        unusedAbilities={analysis.unusedAbilities}
        overlappedStuns={analysis.overlappedStuns}
        midasEfficiency={analysis.midasEfficiency}
        powerTreadsAbilityUsages={analysis.powerTreadsAbilityUsages}
        shardOwners={analysis.shardOwners}
        scepterOwners={analysis.scepterOwners}
      />
      </VStack>
    </div>
  )
}

export default AnalysisComponent