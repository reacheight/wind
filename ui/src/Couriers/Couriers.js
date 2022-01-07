import React from "react";
import styles from '../Analysis/Analysis.module.css'

export default class Couriers extends React.Component {
  render() {
    const courierInfo = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9].map((id) =>
      <li key={id.toString()}>
        <span className={styles.heroName}>{this.props.heroes[id]}</span>'s courier is {this.props.couriers[id] ? <span className={styles.green}>out of</span> : <span className={styles.red}>in</span>} fountain
      </li>
    )

    return (
      <>
        <h5 className={styles.analysisTitle}>Couriers 🐔</h5>
        <ul>{courierInfo}</ul>
      </>
    )
  }
}