import { Analysis } from "../models/Analysis";
import { HeroId } from "../models/HeroId";
import { Team } from "../models/Team";
import Laning from "./categories/Laning/Laning";

import styles from "../styles/AnalysisComponent.module.css"
import Hud from "./categories/Hud/Hud";

interface AnalysisComponentProps {
  targetHero: HeroId
  targetTeam: Team
  analysis: Analysis
}

const AnalysisComponent = ( { targetHero, targetTeam, analysis }: AnalysisComponentProps) => {

  return (
    <div className={styles.analysis}>
      <Laning target={targetHero} couriersState={analysis.couriersState} notTankedCreepwaves={analysis.notTankedCreepwaves} notUnblockedCamps={analysis.notUnblockedCamps} />
      <Hud target={targetHero} unusedItems={analysis.unusedItems} unusedAbilities={analysis.unusedAbilities} overlappedStuns={analysis.overlappedStuns} midasEfficiency={analysis.midasEfficiency} powerTreadsAbilityUsages={analysis.powerTreadsAbilityUsages} />
    </div>
  )
}

export default AnalysisComponent