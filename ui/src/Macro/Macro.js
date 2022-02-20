import styles from "../Analysis/Analysis.module.css";
import {isEmpty} from "../util";
import Observers from "../Observers/Observers";
import Smokes from "../Smokes/Smokes";
import ObserversMaxStock from "../ObserversMaxStock/ObserversMaxStock";
import SmokesMaxStock from "../SmokesMaxStock/SmokesMaxStock";
import WastedCreepwaves from "../WastedCreepwaves/WastedCreepwaves";
import BadFights from "../BadFights/BadFights";
import React from "react";

const Macro = ({ analysis, heroes }) => {
  return (
    <ul className={styles.list}>
      {!isEmpty(analysis.obsPlacedOnVision) &&
        <li>
          <Observers heroes={heroes} observers={analysis.obsPlacedOnVision} />
        </li>}
      {!isEmpty(analysis.smokesUsedOnVision) &&
        <li>
          <Smokes heroes={heroes} smokes={analysis.smokesUsedOnVision} />
        </li>}
      {!isEmpty(analysis.obsMaxCountTime) &&
        <li>
          <ObserversMaxStock observersMaxStock={analysis.obsMaxCountTime} />
        </li>}
      {!isEmpty(analysis.smokeMaxCountTime) &&
        <li>
          <SmokesMaxStock smokesMaxStock={analysis.smokeMaxCountTime} />
        </li>}
      {!isEmpty(analysis.wastedCreepwaves) &&
        <li>
          <WastedCreepwaves wastedCreepwaves={analysis.wastedCreepwaves} />
        </li>}
      {!isEmpty(analysis.badFights) &&
        <li>
          <BadFights badFights={analysis.badFights} />
        </li>}
    </ul>
  )
}

export default Macro