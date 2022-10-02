import { Analysis } from "../models/Analysis";
import styles from "../styles/AnalysisTable.module.css"
import UnusedItemsAnalysis from "./analyzes/UnusedItemsAnalysis";
import UnusedAbilitiesAnalysis from "./analyzes/UnusedAbilitiesAnalysis";
import OverlappedStunsAnalysis from "./analyzes/OverlappedStunsAnalysis";
import NotTankedCreepwavesAnalysis from "./analyzes/NotTankedCreepwavesAnalysis";
import MouseClicksAnalysis from "./analyzes/MouseClicksAnalysis";
import ObserversOnVisionAnalysis from "./analyzes/ObserversOnVisionAnalysis";
import SmokesOnVisionAnalysis from "./analyzes/SmokesOnVisionAnalysis";
import FightsUnderTheWardAnalysis from "./analyzes/FightsUnderTheWardAnalysis";
import BadMapSplitAnalysis from "./analyzes/BadMapSplitAnalysis";
import NotUnblockedCampsAnalysis from "./analyzes/NotUnblockedCampsAnalysis";

interface AnalysisTableProps {
  analysis: Analysis
}

const AnalysisTable = ({ analysis }: AnalysisTableProps) => {
  return (
    <div className={styles.container}>
      <div className={styles.title}>Analysis</div>
      <div className={styles.grid}>
        <div className={styles.column}>
          {analysis.unusedItems.length > 0 && <UnusedItemsAnalysis unusedItems={analysis.unusedItems} />}
          {analysis.notTankedCreepwaves.length > 0 && <NotTankedCreepwavesAnalysis notTankedCreepwaves={analysis.notTankedCreepwaves} />}
          {analysis.observersOnVision.length > 0 && <ObserversOnVisionAnalysis observersOnVision={analysis.observersOnVision} />}
          {analysis.lostFightsUnderTheSameWard.length > 0 && <FightsUnderTheWardAnalysis fightsUnderTheWard={analysis.lostFightsUnderTheSameWard} />}
          {analysis.notUnblockedCamps.length > 0 && <NotUnblockedCampsAnalysis notUnblockedCamps={analysis.notUnblockedCamps} />}
        </div>
        <div className={styles.column}>
          {analysis.unusedAbilities.length > 0 && <UnusedAbilitiesAnalysis unusedAbilities={analysis.unusedAbilities} />}
          {analysis.overlappedStuns.length > 0 && <OverlappedStunsAnalysis overlappedStuns={analysis.overlappedStuns} />}
          {analysis.smokesOnVision.length > 0 && <SmokesOnVisionAnalysis smokesOnVision={analysis.smokesOnVision} />}
          {(analysis.mouseClickItemDeliveries.length > 0 || analysis.mouseClickQuickBuys.length > 0) && <MouseClicksAnalysis mouseClickItemDeliveries={analysis.mouseClickItemDeliveries} mouseClickQuickBuys={analysis.mouseClickQuickBuys} />}
          {analysis.badFights.length > 0 && <BadMapSplitAnalysis badFights={analysis.badFights} />}
        </div>
      </div>
    </div>
  )
}

export default AnalysisTable