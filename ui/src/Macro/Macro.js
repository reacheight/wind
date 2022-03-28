import styles from "../Analysis/Analysis.module.css";
import {isEmpty} from "../util";
import Observers from "../Observers/Observers";
import Smokes from "../Smokes/Smokes";
import ObserversMaxStock from "../ObserversMaxStock/ObserversMaxStock";
import SmokesMaxStock from "../SmokesMaxStock/SmokesMaxStock";
import WastedCreepwaves from "../WastedCreepwaves/WastedCreepwaves";
import BadFights from "../BadFights/BadFights";
import React from "react";
import GlyphOnDeadTower from "../GlyphOnDeadTower/GlyphOnDeadTower";
import BadSmokeFights from "../BadSmokeFights/BadSmokeFights";

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
      {analysis.badSmokeFights.length !== 0 &&
        <li>
          <BadSmokeFights badSmokeFights={analysis.badSmokeFights} />
        </li>
      }
      <li>
        <GlyphOnDeadTower glyphUsedOnDeadT2={analysis.glyphUsedOnDeadT2}/>
      </li>
    </ul>
  )
}

export default Macro