import React from "react";
import styles from "./Resources.module.css";
import { HeroId } from "../../../models/HeroId";
import { SummonGoldFed } from "../../../models/SummonGoldFed";
import SummonGoldFedAnalysis from "./SummonGoldFedAnalysis";

interface ResourcesProps {
  target: HeroId
  summonGoldFed: ReadonlyArray<SummonGoldFed>
}

const Resources = ({ target, summonGoldFed }: ResourcesProps) => {
  const targetSummonGoldFed = summonGoldFed.find(x => x.owner === target)
  if (!targetSummonGoldFed)
    return null

  return (
    <div>
      <span className={styles.title}>Resources</span>
      <div className={styles.grid}>
        <SummonGoldFedAnalysis goldFed={targetSummonGoldFed.gold} />
      </div>
    </div>
  )
}

export default Resources