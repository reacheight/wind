import React from "react";

import { CourierState } from "../../../models/CourierState";
import { NotTankedCreepwave } from "../../../models/NotTankedCreepwave";
import { HeroId } from "../../../models/HeroId";
import { NotUnblockedCamp } from "../../../models/NotUnblockedCamp";
import Courier from "./Courier";

import styles from './Laning.module.css'

interface LaningProps {
  target: HeroId
  couriersState: ReadonlyArray<CourierState>
  notTankedCreepwaves: ReadonlyArray<NotTankedCreepwave>
  notUnblockedCamps: ReadonlyArray<NotUnblockedCamp>
}

const Laning = ({ target, couriersState, notTankedCreepwaves }: LaningProps) => {
  const heroCourierState = couriersState.find(state => state.owner === target)
  const showCourierState = !heroCourierState.isOut && !heroCourierState.isVersusMK

  const courierMessage = <span>Courier was not moved out of fountain.</span>

  const heroCreepwaves = notTankedCreepwaves.filter(entry => entry.heroes.includes(target))
  const showCreepwaves = heroCreepwaves.length > 0
  const creepwavesTimings = heroCreepwaves.map(entry => entry.time).join(", ")
  const creepwavesMessage = <span>Didn't tank {heroCreepwaves.length} creepwaves at {creepwavesTimings}.</span>

  return (
    <div>
      <span className={styles.title}>Laning</span>
      {showCourierState && <Courier />}
    </div>
  )
}

export default Laning