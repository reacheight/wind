import { Analysis } from "../models/Analysis";
import styles from "../styles/AnalysisTable.module.css"
import UnusedItemsAnalysis from "./analyzes/UnusedItemsAnalysis";
import UnusedAbilitiesAnalysis from "./analyzes/UnusedAbilitiesAnalysis";
import OverlappedStunsAnalysis from "./analyzes/OverlappedStunsAnalysis";
import NotTankedCreepwavesAnalysis from "./analyzes/NotTankedCreepwavesAnalysis";
import MouseClicksAnalysis from "./analyzes/MouseClicksAnalysis";
import ObserversOnVisionAnalysis from "./analyzes/ObserversOnVisionAnalysis";

interface AnalysisTableProps {
  analysis: Analysis
}

const AnalysisTable = ({ analysis }: AnalysisTableProps) => {
  return (
    <div className={styles.container}>
      <div className={styles.title}>Analysis</div>
      <div className={styles.grid}>
        {analysis.unusedItems.length > 0 && <UnusedItemsAnalysis unusedItems={analysis.unusedItems} />}
        {analysis.unusedAbilities.length > 0 && <UnusedAbilitiesAnalysis unusedAbilities={analysis.unusedAbilities} />}
        {analysis.overlappedStuns.length > 0 && <OverlappedStunsAnalysis overlappedStuns={analysis.overlappedStuns} />}
        {analysis.notTankedCreepwaves.length > 0 && <NotTankedCreepwavesAnalysis notTankedCreepwaves={analysis.notTankedCreepwaves} />}
        {(analysis.mouseClickItemDeliveries.length > 0 || analysis.mouseClickQuickBuys.length > 0) && <MouseClicksAnalysis mouseClickItemDeliveries={analysis.mouseClickItemDeliveries} mouseClickQuickBuys={analysis.mouseClickQuickBuys} />}
        {analysis.observersOnVision.length > 0 && <ObserversOnVisionAnalysis observersOnVision={analysis.observersOnVision} />}
      </div>
    </div>
  )
}

export default AnalysisTable