import React from 'react';
import styles from '../Analysis/Analysis.module.css'

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

  return (
    <>
      <h5 className={styles.analysisTitle}>Couriers 🐔</h5>
      <ul>{courierInfo}</ul>
    </>
  )
}

export default Couriers