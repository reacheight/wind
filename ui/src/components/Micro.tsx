import styles from "../styles/Analysis.module.css";
import Summons from "./analyzes/Summons";
import UnusedItems from "./analyzes/UnusedItems";
import UnusedAbilities from "./analyzes/UnusedAbilities";
import Couriers from "./analyzes/Couriers";
import MidasEfficiencyComponent from "./analyzes/MidasEfficiency";
import React from "react";
import NotTankedCreepwaves from "./analyzes/NotTankedCreepwaves";
import OverlappedStuns from "./analyzes/OverlappedStuns";
import { Analysis } from "../models/Analysis";

type MicroProps = {
  analysis: Analysis;
}

const Micro = ({ analysis }: MicroProps) => {
  return (
    <ul className={styles.list}>
      {(analysis.unusedItems.length  !== 0) &&
        <li>
          <UnusedItems unusedItems={analysis.unusedItems} />
        </li>}
      {(analysis.unusedAbilities.length !== 0) &&
        <li>
          <UnusedAbilities unusedAbilities={analysis.unusedAbilities} />
        </li>}
      {/*{(!isEmpty(analysis.abilityPt) || !isEmpty(analysis.ptNotOnStrength)) &&*/}
      {/*  <li>*/}
      {/*    <PowerTreads heroes={heroes} powerThreadsAbilityUsage={analysis.abilityPt} ptNotOnStrength={analysis.ptNotOnStrength} />*/}
      {/*  </li>}*/}
      {analysis.overlappedStuns.length !== 0 &&
        <li>
          <OverlappedStuns overlappedStuns={analysis.overlappedStuns} />
        </li>
      }
      {analysis.couriersState.length !== 0 &&
        <li>
          <Couriers couriersState={analysis.couriersState} />
        </li>}
      {analysis.notTankedCreepwaves.length !== 0 &&
        <li>
          <NotTankedCreepwaves notTankedCreepwaves={analysis.notTankedCreepwaves} />
        </li>
      }
      {analysis.summonGoldFed.length !== 0 &&
        <li>
          <Summons summonGoldFed={analysis.summonGoldFed} />
        </li>}
      {analysis.midasEfficiency.length !== 0 &&
        <li>
          <MidasEfficiencyComponent midasEfficiencies={analysis.midasEfficiency} />
        </li>}
    </ul>
  )
}

export default Micro