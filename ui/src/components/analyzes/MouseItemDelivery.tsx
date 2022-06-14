import { MouseClickItemDelivery } from "../../models/MouseClickItemDelivery";
import styles from "../../styles/Analysis.module.css";
import { Heroes } from "../../constants/heroes";
import React from "react";

type MouseItemDeliveryProps = {
  mouseClickItemDeliveries: ReadonlyArray<MouseClickItemDelivery>;
}

const MouseItemDelivery = ({ mouseClickItemDeliveries }: MouseItemDeliveryProps) => {
  const mouseItemDeliveries = mouseClickItemDeliveries.map(({ heroId, count }) => {
    let heroName = <span className={styles.glowing}>{Heroes[heroId]}</span>
    let usageCount = <span className={styles.glowing}>{count}</span>
    return <li key={heroId}>
      {heroName} — {usageCount} times
    </li>
  })

  return (
    <>
      <h5 className={styles.analysisTitle}>Delivering items using <span className={styles.red}>mouse</span> instead of <span className={styles.green}>hotkey</span></h5>
      <ul>{mouseItemDeliveries}</ul>
    </>
  )
}

export default MouseItemDelivery