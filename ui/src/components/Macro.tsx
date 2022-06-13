import styles from "../styles/Analysis.module.css";
import Observers from "./analyzes/Observers";
import Smokes from "./analyzes/Smokes";
import BadFights from "./analyzes/BadFights";
import React from "react";
import GlyphOnDeadTower from "./analyzes/GlyphOnDeadTower";
import { Analysis } from "../models/Analysis";

type MacroProps = {
  analysis: Analysis;
}

const Macro = ({ analysis }: MacroProps) => {
  return (
    <ul className={styles.list}>
      {analysis.observersOnVision.length !== 0 &&
        <li>
          <Observers observersOnVision={analysis.observersOnVision} />
        </li>}
      {analysis.smokesOnVision.length !== 0 &&
        <li>
          <Smokes smokesOnVision={analysis.smokesOnVision} />
        </li>}
      {(analysis.badFights.length !== 0 || analysis.badSmokeFights.length !== 0 || analysis.lostFightsUnderTheSameWard.length !== 0 || analysis.unreasonableDives.length !== 0 || analysis.fightsLostUnderEnemyVision.length !== 0) &&
        <li>
          <BadFights badFights={analysis.badFights} badSmokeFights={analysis.badSmokeFights} lostFightsUnderTheSameWard={analysis.lostFightsUnderTheSameWard} unreasonableDives={analysis.unreasonableDives} fightsLostUnderEnemyVision={analysis.fightsLostUnderEnemyVision} />
        </li>}
      {analysis.worthlessGlyphs.length !== 0 &&
        <li>
          <GlyphOnDeadTower worthlessGlyphs={analysis.worthlessGlyphs}/>
        </li>
      }
    </ul>
  )
}

export default Macro