import React from "react";
import styles from "../Analysis/Analysis.module.css"
import { compareTime } from "../util";

export default class UnusedItems extends React.Component {
  render() {
    const bkbInfo = this.props.deathsWithBKB.map((entry) =>
      [entry[0],
      <li key={"unusedBkb" + entry[0]}>
        <span className={styles.heroName}>{this.props.heroes[entry[1]]}</span> <span className={styles.gray}>died</span> without using <span className={styles.gold}>BKB</span> at {entry[0]}
      </li>]
    )

    const essenceRingInfo = this.props.deathsWithEssenceRing.map((entry) =>
      [entry[0],
      <li key={"unusedER" + entry[0]}>
        <span className={styles.heroName}>{this.props.heroes[entry[1]]}</span> <span className={styles.gray}>died</span> without using <span className={styles.essenceRing}>Essence Ring</span> at {entry[0]}
      </li>]
    )

    const unusedItems = bkbInfo
      .concat(essenceRingInfo)
      .sort((first, second) => compareTime(first[0], second[0]))
      .map(pair => pair[1])

    return (
      <>
        <h5 className={styles.analysisTitle}>Unused items ⚰️</h5>
        <ul>
          {unusedItems}
        </ul>
      </>
    )
  }
}