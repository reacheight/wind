import React from "react";
import styles from '../Analysis/Analysis.module.css'

export default class Observers extends React.Component {
  render() {
    const obsInfo = Object.keys(this.props.observers)
      .map((id) => this.props.observers[id].map((time) => [id, time]))
      .flat()
      .map((pair) =>
        <li key={pair[1]}>
          <span className={styles.heroName}>{this.props.heroes[pair[0]]}</span> at {pair[1]}
        </li>
      )
    
    return (
      <>
        <h5 className={styles.analysisTitle}>Observers placed on enemy vision 👀</h5>
        <ul>{obsInfo}</ul>
      </>
    )
  }
}