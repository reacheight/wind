import React from 'react'
import styles from '../../styles/Analysis.module.css'
import '../../items.css'
import '../../constants/heroes'
import { SmokeOnVision } from "../../models/SmokeOnVision";
import { Heroes } from "../../constants/heroes";

type SmokesOnVisionProps = {
  smokesOnVision: ReadonlyArray<SmokeOnVision>;
}

const Smokes = ({ smokesOnVision }: SmokesOnVisionProps) => {
  const smokes = smokesOnVision
    .map((smoke) =>
      <li key={smoke.time}>
        <span className={styles.glowing}>{Heroes[smoke.hero]}</span> at {smoke.time}
      </li>
    )

  return (
    <>
      <h5 className={styles.analysisTitle}><span className='smoke'>Smokes</span> used on enemy vision 💨</h5>
      <ul>{smokes}</ul>
    </>
  )
}

export default Smokes