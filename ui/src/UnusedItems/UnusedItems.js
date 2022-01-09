import React from "react";
import styles from "../Analysis/Analysis.module.css"

export default class UnusedItems extends React.Component {
  render() {
    const bkbInfo = this.props.deathsWithBKB.map((entry) =>
      <li key={"unusedItem" + entry[0]}>
        <span className={styles.heroName}>{this.props.heroes[entry[1]]}</span> <span className={styles.gray}>died</span> without using <span className={styles.gold}>BKB</span> at {entry[0]}
      </li>
    )

    return (
      <>
        <h5 className={styles.analysisTitle}>Unused items ⚰️</h5>
        <ul>{bkbInfo}</ul>
      </>
    )
  }
}