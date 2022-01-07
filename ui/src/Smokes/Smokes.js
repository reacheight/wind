import React from "react"
import styles from '../Analysis/Analysis.module.css'

export default class Smokes extends React.Component {
  render() {
    const smokeInfo = Object.keys(this.props.smokes)
      .map((id) => this.props.smokes[id].map((time) => [id, time]))
      .flat()
      .map((pair) =>
        <li key={pair[1]}>
          <span className={styles.heroName}>{this.props.heroes[pair[0]]}</span> at {pair[1]}
        </li>
      )
    
    if (smokeInfo.length === 0) {
      return <></>
    }
    
    return (
      <>
        <h5 className={styles.analysisTitle}><span className={styles.smoke}>Smokes</span> used on enemy vision 💨</h5>
        <ul>{smokeInfo}</ul>
      </>
    )
  }
}