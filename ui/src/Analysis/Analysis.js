import React from 'react';
import Couriers from '../Couriers/Couriers';
import Observers from '../Observers/Observers';
import Smokes from '../Smokes/Smokes';
import Summons from '../Summons/Summons';
import { formatHeroName, isEmpty } from '../util';

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
          {!isEmpty(analysis.couriers) &&
          <li>
            <Couriers heroes={heroes} couriers={analysis.couriers} />
          </li>}
          {!isEmpty(analysis.obs_placed_on_vision) &&
          <li>
            <Observers heroes={heroes} observers={analysis.obs_placed_on_vision} />
          </li>}
          {!isEmpty(analysis.smokes_used_on_vision) &&
          <li>
            <Smokes heroes={heroes} smokes={analysis.smokes_used_on_vision} />
          </li>}
          {!isEmpty(analysis.summon_gold) &&
          <li>
            <Summons heroes={heroes} summons={analysis.summon_gold} />
          </li>}
        </ul>
      </div>
    )
  }
}