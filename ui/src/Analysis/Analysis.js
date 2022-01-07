import React from 'react';
import Couriers from '../Couriers/Couriers';
import Observers from '../Observers/Observers';
import Smokes from '../Smokes/Smokes';
import { formatHeroName } from '../util';

import styles from './Analysis.module.css'

export default class Analysis extends React.Component {
  render() {
    const analysis = this.props.analysis

    if (!analysis || Object.keys(analysis).length === 0) {
      return <div></div>
    }

    const heroes = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9].map((id) => formatHeroName(analysis.heroes[id]))

    return (
      <div className={styles.analysis}>
        <ul className={styles.list}>
          <li>
            <Couriers heroes={heroes} couriers={analysis.couriers} />
          </li>
          <li>
            <Observers heroes={heroes} observers={analysis.obs_placed_on_vision} />
          </li>
          <li>
            <Smokes heroes={heroes} smokes={analysis.smokes_used_on_vision} />
          </li>
        </ul>
      </div>
    )
  }
}