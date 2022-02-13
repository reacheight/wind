import styles from "../Analysis/Analysis.module.css";
import {isEmpty} from "../util";
import Observers from "../Observers/Observers";
import Smokes from "../Smokes/Smokes";
import ObserversMaxStock from "../ObserversMaxStock/ObserversMaxStock";
import SmokesMaxStock from "../SmokesMaxStock/SmokesMaxStock";
import WastedCreepwaves from "../WastedCreepwaves/WastedCreepwaves";
import BadFights from "../BadFights/BadFights";
import React from "react";

const Macro = ({ analysis, heroes }) => {
  return (
    <ul className={styles.list}>
      {!isEmpty(analysis.obs_placed_on_vision) &&
        <li>
          <Observers heroes={heroes} observers={analysis.obs_placed_on_vision} />
        </li>}
      {!isEmpty(analysis.smokes_used_on_vision) &&
        <li>
          <Smokes heroes={heroes} smokes={analysis.smokes_used_on_vision} />
        </li>}
      {!isEmpty(analysis.obs_max_count_time) &&
        <li>
          <ObserversMaxStock observersMaxStock={analysis.obs_max_count_time} />
        </li>}
      {!isEmpty(analysis.smoke_max_count_time) &&
        <li>
          <SmokesMaxStock smokesMaxStock={analysis.smoke_max_count_time} />
        </li>}
      {!isEmpty(analysis.wastedCreepwaves) &&
        <li>
          <WastedCreepwaves wastedCreepwaves={analysis.wastedCreepwaves} />
        </li>}
      {!isEmpty(analysis.badFights) &&
        <li>
          <BadFights badFights={analysis.badFights} />
        </li>}
    </ul>
  )
}

export default Macro