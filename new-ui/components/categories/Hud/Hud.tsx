import { UnusedItem } from "../../../models/UnusedItem";
import { UnusedAbility } from "../../../models/UnusedAbility";
import { OverlappedStun } from "../../../models/OverlappedStun";
import { MidasEfficiency } from "../../../models/MidasEfficiency";
import { PowerTreadsAbilityUsages } from "../../../models/PowerTreadsAbilityUsages";
import { HeroId } from "../../../models/HeroId";

import styles from "./Hud.module.css"
import Image from "next/image";
import Routes from "../../../api/routs";

interface HudProps {
  target: HeroId
  unusedItems: ReadonlyArray<UnusedItem>
  unusedAbilities: ReadonlyArray<UnusedAbility>
  overlappedStuns: ReadonlyArray<OverlappedStun>
  midasEfficiency: ReadonlyArray<MidasEfficiency>
  powerTreadsAbilityUsages: ReadonlyArray<PowerTreadsAbilityUsages>
}

const Hud = ({ target, unusedItems, unusedAbilities, overlappedStuns, midasEfficiency, powerTreadsAbilityUsages }: HudProps) => {
  const targetUnusedItems = unusedItems.filter(unusedItem => unusedItem.user === target)
  const targetUnusedAbilities = unusedAbilities.filter(unusedAbility => unusedAbility.user === target)
  const targetOverlappedStuns = overlappedStuns.filter(overlappedStun => overlappedStun.user === target)
  const targetMidasEfficiency = midasEfficiency.filter(me => me.hero === target)
  const targetPTUsages = powerTreadsAbilityUsages.filter(usage => usage.heroId === target)

  return (
    <div>
      <span className={styles.title}>Abilities & Items usage</span>
      <div>
        <div className={styles.portrait}>
          <Image src={Routes.Images.getVerticalPortrait(target)} width={142} height={188} />
        </div>
      </div>
    </div>
  )
}

export default Hud