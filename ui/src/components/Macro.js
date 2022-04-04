import styles from "../styles/Analysis.module.css";
import {isEmpty} from "../util";
import Observers from "./analyzes/Observers";
import Smokes from "./analyzes/Smokes";
import BadFights from "./analyzes/BadFights";
import React from "react";
import GlyphOnDeadTower from "./analyzes/GlyphOnDeadTower";
import BadSmokeFights from "./analyzes/BadSmokeFights";

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