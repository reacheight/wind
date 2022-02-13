import styles from "../Analysis/Analysis.module.css";
import {isEmpty} from "../util";
import PowerTreads from "../PowerThreads/PowerTreads";
import Summons from "../Summons/Summons";
import UnusedItems from "../UnusedItems/UnusedItems";
import UnusedAbilities from "../UnusedAbilities/UnusedAbilities";
import Couriers from "../Couriers/Couriers";
import MidasEfficiency from "../MidassEfficiency/MidasEfficiency";
import React from "react";

const Micro = ({ analysis, heroes }) => {
  return (
    <ul className={styles.list}>
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
      {!isEmpty(analysis.midasEfficiency) &&
        <li>
          <MidasEfficiency heroes={heroes} midasEfficiency={analysis.midasEfficiency} />
        </li>}
    </ul>
  )
}

export default Micro