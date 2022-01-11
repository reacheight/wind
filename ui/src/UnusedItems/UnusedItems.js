import React from "react";
import styles from "../Analysis/Analysis.module.css"
import "../items.css"
import { getItemClassName } from "../util";

export default class UnusedItems extends React.Component {
  render() {
    const unusedItemsInfo = this.props.unusedItems.map(entry =>
      <li key={"unused" + entry[2] + entry[0]}>
        <span className={styles.heroName}>{this.props.heroes[entry[1]]}</span> <span className={styles.gray}>died</span> without using <span className={getItemClassName(entry[2])}>{entry[2]}</span> at {entry[0]}
      </li>
    )

    return (
      <>
        <h5 className={styles.analysisTitle}>Unused items ⚰️</h5>
        <ul>
          {unusedItemsInfo}
        </ul>
      </>
    )
  }
}