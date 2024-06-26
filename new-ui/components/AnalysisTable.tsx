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
import ItemBuildsAnalysis from "./analyzes/ItemBuildsAnalysis";
import PoorMapAwarenessAnalysis from "./analyzes/PoorMapAwarenessAnalysis";
import PowerTreadsAnalysis from "./analyzes/PowerTreadsAnalysis";

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
          {analysis.lostFightsUnderTheSameWard.length > 0 && <FightsUnderTheWardAnalysis fightsUnderTheWard={analysis.lostFightsUnderTheSameWard} />}
          {analysis.badSmokeFights.length > 0 && <PoorMapAwarenessAnalysis badSmokeFights={analysis.badSmokeFights} />}
          {analysis.powerTreadsAbilityUsages.length > 0 && <PowerTreadsAnalysis powerTreadsAbilityUsages={analysis.powerTreadsAbilityUsages} />}
          {analysis.notUnblockedCamps.length > 0 && <NotUnblockedCampsAnalysis notUnblockedCamps={analysis.notUnblockedCamps} />}
          {analysis.observersOnVision.length > 0 && <ObserversOnVisionAnalysis observersOnVision={analysis.observersOnVision} />}
        </div>
        <div className={styles.column}>
          {analysis.unusedAbilities.length > 0 && <UnusedAbilitiesAnalysis unusedAbilities={analysis.unusedAbilities} />}
          {analysis.overlappedStuns.length > 0 && <OverlappedStunsAnalysis overlappedStuns={analysis.overlappedStuns} />}
          {analysis.notPurchasedSticks.length > 0 || analysis.notPurchasedItemAgainstHero.length > 0 && <ItemBuildsAnalysis notPurchasedSticks={analysis.notPurchasedSticks} notPurchasedItemAgainstHero={analysis.notPurchasedItemAgainstHero} />}
          {analysis.smokesOnVision.length > 0 && <SmokesOnVisionAnalysis smokesOnVision={analysis.smokesOnVision} />}
          {(analysis.mouseClickItemDeliveries.length > 0 || analysis.mouseClickQuickBuys.length > 0) && <MouseClicksAnalysis mouseClickItemDeliveries={analysis.mouseClickItemDeliveries} mouseClickQuickBuys={analysis.mouseClickQuickBuys} />}
          {analysis.badFights.length > 0 && <BadMapSplitAnalysis badFights={analysis.badFights} />}
        </div>
      </div>
    </div>
  )
}

export default AnalysisTable