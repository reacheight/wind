import React from "react";
import styles from '../Analysis/Analysis.module.css'
import { compareTime } from "../util";

export default class Observers extends React.Component {
  render() {
    const obsInfo = Object.keys(this.props.observers)
      .map((id) => this.props.observers[id].map((time) => [id, time]))
      .flat()
      .sort((first, second) => compareTime(first[1], second[1]))
      .map((pair) =>
        <li key={pair[1]}>
          <span className={styles.heroName}>{this.props.heroes[pair[0]]}</span> at {pair[1]}
        </li>
      )
    
    if (obsInfo.length === 0) {
      return <></>
    }
    
    return (
      <>
        <h5 className={styles.analysisTitle}><span className={styles.observer}>Observers</span> placed on enemy vision 👀</h5>
        <ul>{obsInfo}</ul>
      </>
    )
  }
}