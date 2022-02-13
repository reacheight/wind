import React from 'react';
import Couriers from '../Couriers/Couriers';
import ItemTimings from '../ItemTimings/ItemTimings';
import Observers from '../Observers/Observers';
import ObserversMaxStock from '../ObserversMaxStock/ObserversMaxStock';
import PowerTreads from '../PowerThreads/PowerTreads';
import Smokes from '../Smokes/Smokes';
import SmokesMaxStock from '../SmokesMaxStock/SmokesMaxStock';
import Summons from '../Summons/Summons';
import UnusedAbilities from '../UnusedAbilities/UnusedAbilities';
import UnusedItems from '../UnusedItems/UnusedItems';
import { formatHeroName, formatName, isEmpty } from '../util';

import styles from './Analysis.module.css'
import MidasEfficiency from "../MidassEfficiency/MidasEfficiency";
import WastedCreepwaves from "../WastedCreepwaves/WastedCreepwaves";
import BadFights from "../BadFights/BadFights";

export default class Analysis extends React.Component {
  render() {
    const analysis = this.props.analysis

    if (!analysis || Object.keys(analysis).length === 0) {
      return <div></div>
    }

    const heroes = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9].map((id) => formatHeroName(analysis.heroes[id]))
    const itemTimings = Object.keys(analysis.purchases).map(hero =>
      <li key={"timings" + hero}>
        <ItemTimings hero={hero} purchases={analysis.purchases[hero]} />
      </li>
    )

    return (
      <div className={styles.analysis}>
        <ul className={styles.list}>
          {!isEmpty(analysis.obs_placed_on_vision) &&
          <li>
            <Observers heroes={heroes} observers={analysis.obs_placed_on_vision} />
          </li>}
          {!isEmpty(analysis.smokes_used_on_vision) &&
          <li>
            <Smokes heroes={heroes} smokes={analysis.smokes_used_on_vision} />
          </li>}
          {(!isEmpty(analysis.ability_pt) || !isEmpty(analysis.ptNotOnStrength)) &&
          <li>
            <PowerTreads heroes={heroes} powerThreadsAbilityUsage={analysis.ability_pt} ptNotOnStrength={analysis.ptNotOnStrength} />
          </li>}
          {!isEmpty(analysis.summon_gold) &&
          <li>
            <Summons heroes={heroes} summons={analysis.summon_gold} />
          </li>}
          {(!isEmpty(analysis.unusedItems) || !isEmpty(analysis.unusedOnAllyAbilities)) &&
          <li>
            <UnusedItems heroes={heroes} unusedItems={analysis.unusedItems} />
          </li>}
          {!isEmpty(analysis.unusedAbilities) &&
          <li>
            <UnusedAbilities heroes={heroes} unusedAbilities={analysis.unusedAbilities} unusedOnAllyAbilities={analysis.unusedOnAllyAbilities} />
          </li>}
          {!isEmpty(analysis.couriers) &&
          <li>
            <Couriers heroes={heroes} couriers={analysis.couriers} />
          </li>}
          {!isEmpty(analysis.obs_max_count_time) &&
          <li>
            <ObserversMaxStock observersMaxStock={analysis.obs_max_count_time} />
          </li>}
          {!isEmpty(analysis.smoke_max_count_time) &&
          <li>
            <SmokesMaxStock smokesMaxStock={analysis.smoke_max_count_time} />
          </li>}
          {!isEmpty(analysis.midasEfficiency) &&
          <li>
            <MidasEfficiency heroes={heroes} midasEfficiency={analysis.midasEfficiency} />
          </li>}
          {!isEmpty(analysis.wastedCreepwaves) &&
          <li>
            <WastedCreepwaves wastedCreepwaves={analysis.wastedCreepwaves} />
          </li>}
          {!isEmpty(analysis.badFights) &&
          <li>
            <BadFights badFights={analysis.badFights} />
          </li>}
          {itemTimings}
        </ul>
      </div>
    )
  }
}