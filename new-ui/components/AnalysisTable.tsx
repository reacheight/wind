import { Analysis } from "../models/Analysis";
import styles from "../styles/AnalysisTable.module.css"
import UnusedItemsAnalysis from "./analyzes/UnusedItemsAnalysis";

interface AnalysisTableProps {
  analysis: Analysis
}

const AnalysisTable = ({ analysis }: AnalysisTableProps) => {
  return (
    <div className={styles.main}>
      <div className={styles.title}>Analysis</div>
      <UnusedItemsAnalysis unusedItems={analysis.unusedItems} />
    </div>
  )
}

export default AnalysisTable