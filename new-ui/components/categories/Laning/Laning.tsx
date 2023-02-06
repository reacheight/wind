import React from "react";

import { CourierState } from "../../../models/CourierState";
import { NotTankedCreepwave } from "../../../models/NotTankedCreepwave";
import { HeroId } from "../../../models/HeroId";
import { NotUnblockedCamp } from "../../../models/NotUnblockedCamp";
import Courier from "./Courier";

import styles from './Laning.module.css'
import NotUnblockedCamps from "./NotUnblockedCamps";
import NotTankedCreepwaves from "./NotTankedCreepwaves";

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

  const targetNotTankedCreepwaves = notTankedCreepwaves.filter(entry => entry.heroes.includes(target))
  const showNotTankedCreepwaves = targetNotTankedCreepwaves.length !== 0

  const showCategory = showCourierState || targetNotUnblockedCamp || showNotTankedCreepwaves
  if (!showCategory)
    return null

  return (
    <div>
      <span className={styles.title}>Laning</span>
      <div className={styles.grid}>
        {showCourierState && <Courier />}
        {targetNotUnblockedCamp && <NotUnblockedCamps notUnblockedCamp={targetNotUnblockedCamp} />}
        {notTankedCreepwaves && <NotTankedCreepwaves notTankedCreepwaves={targetNotTankedCreepwaves} />}
      </div>
    </div>
  )
}

export default Laning