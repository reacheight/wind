import styles from "../../styles/Analysis.module.css";
import React from "react";
import { WorthlessGlyph } from "../../models/WorthlessGlyph";
import { Team } from "../../models/Team";

type WorthlessGlyphProps = {
  worthlessGlyphs: ReadonlyArray<WorthlessGlyph>;
}

const GlyphOnDeadTower = ({ worthlessGlyphs }: WorthlessGlyphProps) => {
  const glyphs = worthlessGlyphs.map(glyph =>
    <li key={glyph.team}>
      <span className={styles[Team[glyph.team]]}>{Team[glyph.team]}</span> at {glyph.times.join(", ")}
    </li>
  )

  return <>
    <h5 className={styles.analysisTitle}>Glyph was used, but T2 died anyway</h5>
    <ul>{glyphs}</ul>
  </>
}

export default GlyphOnDeadTower