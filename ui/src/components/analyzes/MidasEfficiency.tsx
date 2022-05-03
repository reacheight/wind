import styles from '../../styles/Analysis.module.css'
import React from "react";
import { MidasEfficiency } from "../../models/MidasEfficiency";
import { Heroes } from "../../constants/heroes";

type MidasEfficiencyProps = {
  midasEfficiencies: ReadonlyArray<MidasEfficiency>;
}

const MidasEfficiencyComponent = ({ midasEfficiencies }: MidasEfficiencyProps) => {
  const efficiencyList = midasEfficiencies.map(efficiency =>
    <li key={efficiency.hero}>
      <span className={styles.glowing}>{Heroes[efficiency.hero]}</span> — {efficiency.efficiency.toFixed(2)}
    </li>
  )

  return (
    <>
      <h5 className={styles.analysisTitle}>Midas efficiency</h5>
      <ul>{efficiencyList}</ul>
    </>
  )
}

export default MidasEfficiencyComponent