import React from "react";
import styles from "../Analysis/Analysis.module.css"

export default class Summons extends React.Component {
  render() {
    const summonInfo = Object.keys(this.props.summons).map((id) =>
      <li key={"summon" + id.toString()}>
        {this.props.summons[id]} by <span className={styles.heroName}>{this.props.heroes[id]}</span>
      </li>
    )

    if (summonInfo.length === 0) {
      return <></>
    }

    return (
      <>
        <h5 className={styles.analysisTitle}>Gold fed with summons 🪙</h5>
        <ul>{summonInfo}</ul>
      </>
    )
  }
}