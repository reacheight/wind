import React from 'react';
import styles from '../../styles/Analysis.module.css'
import AnalysisHint from "../AnalysisHint";
import { CourierState } from "../../models/CourierState";
import { Heroes } from "../../constants/heroes";

type CouriersProps = {
  couriersState: ReadonlyArray<CourierState>;
}

const Couriers = ({ couriersState }: CouriersProps) => {
  const couriers = couriersState.map(({ owner, isOut, isVersusMK }) =>
    <li key={owner}>
      <span className={styles.glowing}>{Heroes[owner]}</span>'s courier is {isOut ? <span className={styles.green}>out of</span> : <span className={styles.red}>in</span>} fountain
      {(isOut && !isVersusMK) && <> ✔️️️️</>}
      {(isOut && isVersusMK) && <> VS <span className={styles.glowing}>Monkey King</span> 🤡</>}
      {(!isOut && !isVersusMK) && <> ❌</>}
      {(!isOut && isVersusMK) && <> VS <span className={styles.glowing}>Monkey King</span> ✔️️</>}
    </li>
  )

  let hintContent =
    "Moving your courier slightly out of your fountain at the start of the game is a little trick that can save you a second or two on item delivery. " +
    "Just keep it in the shop range. And be careful when you're playing against Monkey King as he can sneak to your base and kill your chickens."
  return (
    <>
      <h5 className={styles.analysisTitle}>Couriers 🐔</h5>
      <AnalysisHint hint={hintContent} />
      <ul>{couriers}</ul>
    </>
  )
}

export default Couriers