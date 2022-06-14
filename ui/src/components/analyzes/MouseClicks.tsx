import { MouseClickItemDelivery } from "../../models/MouseClickItemDelivery";
import styles from "../../styles/Analysis.module.css";
import { Heroes } from "../../constants/heroes";
import React from "react";
import { MouseClickQuickBuy } from "../../models/MouseClickQuickBuy";

type MouseClicksProps = {
  mouseClickItemDeliveries: ReadonlyArray<MouseClickItemDelivery>;
  mouseClickQuickBuys: ReadonlyArray<MouseClickQuickBuy>;
}

const MouseClicks = ({ mouseClickItemDeliveries, mouseClickQuickBuys }: MouseClicksProps) => {
  const mouseItemDeliveries = mouseClickItemDeliveries.map(({ heroId, count }) => {
    let heroName = <span className={styles.glowing}>{Heroes[heroId]}</span>
    let usageCount = <span className={styles.glowing}>{count}</span>
    return <li key={heroId}>
      {heroName} delivered items {usageCount} times
    </li>
  })

  const mouseQuickBuys = mouseClickQuickBuys.map(({ heroId, count }) => {
    let heroName = <span className={styles.glowing}>{Heroes[heroId]}</span>
    let usageCount = <span className={styles.glowing}>{count}</span>
    return <li key={heroId}>
      {heroName} quick-bought {usageCount} times
    </li>
  })

  return (
    <>
      <h5 className={styles.analysisTitle}>Using <span className={styles.red}>mouse</span> instead of <span className={styles.green}>hotkey</span></h5>
      <ul>{mouseItemDeliveries}</ul>
      <ul>{mouseQuickBuys}</ul>
    </>
  )
}

export default MouseClicks