import styles from "../styles/Analysis.module.css";
import {isEmpty} from "../util";
import PowerTreads from "./analyzes/PowerTreads";
import Summons from "./analyzes/Summons";
import UnusedItems from "./analyzes/UnusedItems";
import UnusedAbilities from "./analyzes/UnusedAbilities";
import Couriers from "./analyzes/Couriers";
import MidasEfficiency from "./analyzes/MidasEfficiency";
import React from "react";
import NotTankedCreepwaves from "./analyzes/NotTankedCreepwaves";
import OverlappedStuns from "./analyzes/OverlappedStuns";

const Micro = ({ analysis, heroes }) => {
  return (
    <ul className={styles.list}>
      {(analysis.unusedItems.length + analysis.unusedOnAllyItems.length !== 0) &&
        <li>
          <UnusedItems heroes={heroes} unusedItems={analysis.unusedItems} unusedOnAllyItems={analysis.unusedOnAllyItems} />
        </li>}
      {(analysis.unusedAbilities.length + analysis.unusedOnAllyAbilities.length + analysis.unusedOnAllyWithBlinkAbilities.length !== 0) &&
        <li>
          <UnusedAbilities heroes={heroes} unusedAbilities={analysis.unusedAbilities} unusedOnAllyAbilities={analysis.unusedOnAllyAbilities} unusedOnAllyWithBlinkAbilities={analysis.unusedOnAllyWithBlinkAbilities} />
        </li>}
      {/*{(!isEmpty(analysis.abilityPt) || !isEmpty(analysis.ptNotOnStrength)) &&*/}
      {/*  <li>*/}
      {/*    <PowerTreads heroes={heroes} powerThreadsAbilityUsage={analysis.abilityPt} ptNotOnStrength={analysis.ptNotOnStrength} />*/}
      {/*  </li>}*/}
      {(analysis.overlappedStuns && analysis.overlappedStuns.length !== 0) &&
        <li>
          <OverlappedStuns overlappedStuns={analysis.overlappedStuns} heroes={heroes} />
        </li>
      }
      {!isEmpty(analysis.couriers) &&
        <li>
          <Couriers heroes={heroes} couriers={analysis.couriers} />
        </li>}
      {!isEmpty(analysis.notTankedCreepwaves) &&
        <li>
          <NotTankedCreepwaves notTankedCreepwaves={analysis.notTankedCreepwaves} heroes={heroes} />
        </li>
      }
      {!isEmpty(analysis.summonGold) &&
        <li>
          <Summons heroes={heroes} summons={analysis.summonGold} />
        </li>}
      {!isEmpty(analysis.midasEfficiency) &&
        <li>
          <MidasEfficiency heroes={heroes} midasEfficiency={analysis.midasEfficiency} />
        </li>}
    </ul>
  )
}

export default Micro