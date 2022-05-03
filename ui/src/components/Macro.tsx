import styles from "../styles/Analysis.module.css";
import Observers from "./analyzes/Observers";
import Smokes from "./analyzes/Smokes";
import BadFights from "./analyzes/BadFights";
import React from "react";
import GlyphOnDeadTower from "./analyzes/GlyphOnDeadTower";
import BadSmokeFights from "./analyzes/BadSmokeFights";
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
      {analysis.outnumberedFights.length !== 0 &&
        <li>
          <BadFights outnumberedFights={analysis.outnumberedFights} />
        </li>}
      {analysis.badSmokeFights.length !== 0 &&
        <li>
          <BadSmokeFights badSmokeFights={analysis.badSmokeFights} />
        </li>
      }
      {analysis.worthlessGlyphs.length !== 0 &&
        <li>
          <GlyphOnDeadTower worthlessGlyphs={analysis.worthlessGlyphs}/>
        </li>
      }
    </ul>
  )
}

export default Macro