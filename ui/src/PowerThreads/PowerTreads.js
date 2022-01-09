import React from "react";
import styles from '../Analysis/Analysis.module.css'

export default class PowerTreads extends React.Component {
  render() {
    const ptInfo = Object.keys(this.props.powerThreadsAbilityUsage).map((id) => {
      let usageCount = this.props.powerThreadsAbilityUsage[id]
      return <li key={"ptAbility" + id}>
        <span className={styles.heroName}>{this.props.heroes[id]}</span> used {usageCount[0]} spells total with PT, {usageCount[1]} on <span className={styles.intelligence}>Intelligence</span>
      </li>
    })

    return (
      <>
        <h5 className={styles.analysisTitle}>Switching Power Treads</h5>
        <ul>{ptInfo}</ul>
      </>
    )
  }
}