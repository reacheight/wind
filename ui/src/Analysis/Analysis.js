import React from 'react';
import { formatHeroName } from '../util';

import styles from './Analysis.module.css'

export default class Analysis extends React.Component {
  render() {
    const analysis = this.props.analysis

    if (!analysis || Object.keys(analysis).length === 0) {
      return <div></div>
    }

    const courierInfo = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9].map((id) =>
      <li key={id.toString}>
        <span className={styles.heroName}>{formatHeroName(analysis.heroes[id])}</span>'s courier is {analysis.couriers[id] ? <span className={styles.green}>out of</span> : <span className={styles.red}>in</span>} fountain
      </li>
    )

    const observersOnVision = analysis.obs_placed_on_vision
    const obsInfo = Object.keys(observersOnVision).map((id) =>
      observersOnVision[id].map((time) => [id, time])
    ).flat().map((pair) =>
      <li key={pair[1]}>
        <span className={styles.heroName}>{formatHeroName(analysis.heroes[pair[0]])}</span> at {pair[1]}
      </li>
    )
    
    return (
      <div className={styles.analysis}>
        <ul className={styles.list}>
          <li>
            <h5 className={styles.analysisTitle}>Couriers 🐔</h5>
            <ul>{courierInfo}</ul>
          </li>
          <li>
            <h5 className={styles.analysisTitle}>Observers placed on enemy vision 👀</h5>
            <ul>{obsInfo}</ul>
          </li>
        </ul>
      </div>
    )
  }
}