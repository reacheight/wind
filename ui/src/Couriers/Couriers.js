import React from 'react';
import styles from '../Analysis/Analysis.module.css'

const Couriers = (props) => {
  const courierInfo = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9].map(i => i * 2).map((id) =>
    <li key={id.toString()}>
      <span className={styles.heroName}>{props.heroes[id]}</span>'s courier is {props.couriers[id] ? <span className={styles.green}>out of</span> : <span className={styles.red}>in</span>} fountain
    </li>
  )

  return (
    <>
      <h5 className={styles.analysisTitle}>Couriers 🐔</h5>
      <ul>{courierInfo}</ul>
    </>
  )
}

export default Couriers