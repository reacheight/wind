import styles from "../Analysis/Analysis.module.css";
import React from "react";

const GlyphOnDeadTower = ({glyphUsedOnDeadT2}) => {
  if (glyphUsedOnDeadT2[0].concat(glyphUsedOnDeadT2[1]).length === 0)
    return <></>

  return <>
    <h5 className={styles.analysisTitle}>Glyph was used, but T2 died anyway</h5>
    <ul>
      {glyphUsedOnDeadT2[0].length > 0 &&
        <li key={'uselessGlyphRadiant'}>
          <span className={styles.green}>Radiant</span> at {glyphUsedOnDeadT2[0].join(", ")}
        </li>
      }
      {glyphUsedOnDeadT2[1].length > 0 &&
        <li key={'uselessGlyphDire'}>
          <span className={styles.red}>Dire</span> at {glyphUsedOnDeadT2[1].join(", ")}
        </li>
      }
    </ul>
  </>
}

export default GlyphOnDeadTower