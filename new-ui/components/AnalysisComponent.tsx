import { Analysis } from "../models/Analysis";
import { HeroId } from "../models/HeroId";
import { Team } from "../models/Team";
import Laning from "./categories/Laning/Laning";

import styles from "../styles/AnalysisComponent.module.css"

interface AnalysisComponentProps {
  targetHero: HeroId
  targetTeam: Team
  analysis: Analysis
}

const AnalysisComponent = ( { targetHero, targetTeam, analysis }: AnalysisComponentProps) => {

  return (
    <div className={styles.analysis}>
      <Laning target={targetHero} couriersState={analysis.couriersState} notTankedCreepwaves={analysis.notTankedCreepwaves} notUnblockedCamps={analysis.notUnblockedCamps} />
    </div>
  )
}

export default AnalysisComponent