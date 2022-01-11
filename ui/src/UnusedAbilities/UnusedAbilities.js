import React from "react";
import styles from "../Analysis/Analysis.module.css"

export default class UnusedAbilities extends React.Component {
  render() {
    const unusedAbilitiesInfo = this.props.unusedAbilities.map(entry =>
      <li key={"unused" + entry[2] + entry[0]}>
        <span className={styles.heroName}>{this.props.heroes[entry[1]]}</span> <span className={styles.gray}>died</span> without using {entry[2]} at {entry[0]}
      </li>
    )

    return (
      <>
        <h5 className={styles.analysisTitle}>Unused abilities ☠️</h5>
        <ul>{unusedAbilitiesInfo}</ul>
      </>
    )
  }
}