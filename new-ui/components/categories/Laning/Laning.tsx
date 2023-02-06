import React from "react";

import { CourierState } from "../../../models/CourierState";
import { NotTankedCreepwave } from "../../../models/NotTankedCreepwave";
import { HeroId } from "../../../models/HeroId";
import { NotUnblockedCamp } from "../../../models/NotUnblockedCamp";
import Courier from "./Courier";

import styles from './Laning.module.css'
import NotUnblockedCamps from "./NotUnblockedCamps";

interface LaningProps {
  target: HeroId
  couriersState: ReadonlyArray<CourierState>
  notTankedCreepwaves: ReadonlyArray<NotTankedCreepwave>
  notUnblockedCamps: ReadonlyArray<NotUnblockedCamp>
}

const Laning = ({ target, couriersState, notTankedCreepwaves, notUnblockedCamps }: LaningProps) => {
  const heroCourierState = couriersState.find(state => state.owner === target)
  const showCourierState = !heroCourierState.isOut && !heroCourierState.isVersusMK

  const targetNotUnblockedCamp = notUnblockedCamps.find(entry => entry.heroId === target)

  return (
    <div>
      <span className={styles.title}>Laning</span>
      <div className={styles.grid}>
        {showCourierState && <Courier />}
        {targetNotUnblockedCamp && <NotUnblockedCamps notUnblockedCamp={targetNotUnblockedCamp} />}
      </div>
    </div>
  )
}

export default Laning