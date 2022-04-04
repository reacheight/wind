import React from 'react';
import styles from '../../styles/Analysis.module.css'
import AnalysisHint from "../AnalysisHint";

const Couriers = (props) => {
  const courierInfo = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9].map(i => i * 2).map((id) => {
      let [isOut, versusMK] = props.couriers[id]
      return <li key={"couriers" + id.toString()}>
        <span className={styles.glowing}>{props.heroes[id]}</span>'s courier is {isOut ? <span className={styles.green}>out of</span> : <span className={styles.red}>in</span>} fountain
        {(isOut && !versusMK) && <> ✔️️️️</>}
        {(isOut && versusMK) && <> VS <span className={styles.glowing}>Monkey King</span> 🤡</>}
        {(!isOut && !versusMK) && <> ❌</>}
        {(!isOut && versusMK) && <> VS <span className={styles.glowing}>Monkey King</span> ✔️️</>}
      </li>
    }
  )

  let hintContent = <span>
    Moving your courier slightly out of your fountain at the start of the game is a little trick that can save you a second or two on item delivery.
    Just keep it in the shop range. And be careful when you're playing against Monkey King as he can sneak to your base and kill your chickens.
  </span>
  return (
    <>
      <h5 className={styles.analysisTitle}>Couriers 🐔</h5>
      <AnalysisHint hint={hintContent} />
      <ul>{courierInfo}</ul>
    </>
  )
}

export default Couriers