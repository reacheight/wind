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
        {formatHeroName(analysis.heroes[id])}'s courier is {analysis.couriers[id] ? <span className={styles.green}>out of</span> : <span className={styles.red}>in</span>} fountain
      </li>
    )
    
    return (
      <div className={styles.analysis}>
        <ul>{courierInfo}</ul>
      </div>
    )
  }
}