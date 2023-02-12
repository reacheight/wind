import { UnusedItem } from "../../../models/UnusedItem";
import { UnusedAbility } from "../../../models/UnusedAbility";
import { OverlappedStun } from "../../../models/OverlappedStun";
import { MidasEfficiency } from "../../../models/MidasEfficiency";
import { PowerTreadsAbilityUsages } from "../../../models/PowerTreadsAbilityUsages";
import { HeroId } from "../../../models/HeroId";
import Image from "next/image";
import Routes from "../../../api/routs";
import { useEffect, useState } from "react";
import { HeroAbility } from "../../../models/HeroAbility";
import AbilitiesPanel from "./AbilitiesPanel";
import UsageInfo from "./UsageInfo";

import styles from "./Hud.module.css"

interface HudProps {
  target: HeroId
  unusedItems: ReadonlyArray<UnusedItem>
  unusedAbilities: ReadonlyArray<UnusedAbility>
  overlappedStuns: ReadonlyArray<OverlappedStun>
  midasEfficiency: ReadonlyArray<MidasEfficiency>
  powerTreadsAbilityUsages: ReadonlyArray<PowerTreadsAbilityUsages>
  shardOwners: ReadonlyArray<HeroId>
  scepterOwners: ReadonlyArray<HeroId>
}

const Hud = ({ target, unusedItems, unusedAbilities, overlappedStuns, midasEfficiency, powerTreadsAbilityUsages, scepterOwners, shardOwners }: HudProps) => {
  const [abilities, setAbilities] = useState<ReadonlyArray<HeroAbility>>(null)
  const [selectedAbilityId, setSelectedAbilityId] = useState(null)

  const onAbilityClick = (abilityId) => {
    if (selectedAbilityId === abilityId) {
      setSelectedAbilityId(null)
    }
    else {
      setSelectedAbilityId(abilityId)
    }
  }

  useEffect(() => {
    setAbilities(null)
    setSelectedAbilityId(null)

    fetch(Routes.Constants.getHeroAbilities(target))
      .then(response => response.json())
      .then(abilities => setAbilities(abilities))
  }, [target])

  if (abilities === null)
    return null;

  const targetUnusedItems = unusedItems.filter(unusedItem => unusedItem.user === target)
  const targetUnusedAbilities = unusedAbilities.filter(unusedAbility => unusedAbility.user === target)
  const targetOverlappedStuns = overlappedStuns.filter(overlappedStun => overlappedStun.user === target)
  const targetMidasEfficiency = midasEfficiency.filter(me => me.hero === target)
  const targetPTUsages = powerTreadsAbilityUsages.filter(usage => usage.heroId === target)

  const targetAbilities = abilities.filter(ability => ability.slot <= 6)
    .filter(ability => ability.id !== 6251)
    .filter(ability => !ability.isGrantedByShard || shardOwners.includes(target))
    .filter(ability => !ability.isGrantedByScepter || scepterOwners.includes(target))

  return (
    <div>
      <span className={styles.title}>Abilities & Items usage</span>
      <div className={styles.hud}>
        <div className={styles.portrait}>
          <Image src={Routes.Images.getVerticalPortrait(target)} width={142} height={188} />
        </div>
        <AbilitiesPanel selectedAbility={selectedAbilityId} onAbilityClick={id => onAbilityClick(id)} abilities={targetAbilities} />
        <div>

        </div>
      </div>
      <UsageInfo selectedAbilityId={selectedAbilityId} abilities={targetAbilities} unusedAbilities={targetUnusedAbilities} overlappedStuns={targetOverlappedStuns} />
    </div>
  )
}

export default Hud