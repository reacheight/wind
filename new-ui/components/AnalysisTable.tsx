import { Analysis } from "../models/Analysis";
import styles from "../styles/AnalysisTable.module.css"
import UnusedItemsAnalysis from "./analyzes/UnusedItemsAnalysis";
import UnusedAbilitiesAnalysis from "./analyzes/UnusedAbilitiesAnalysis";
import OverlappedStunsAnalysis from "./analyzes/OverlappedStunsAnalysis";
import NotTankedCreepwavesAnalysis from "./analyzes/NotTankedCreepwavesAnalysis";

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
      </div>
    </div>
  )
}

export default AnalysisTable