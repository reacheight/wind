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
import { HStack } from "@chakra-ui/layout";

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
  const targetUnusedItems = unusedItems.filter(unusedItem => unusedItem.user === target)
  const targetUnusedAbilities = unusedAbilities.filter(unusedAbility => unusedAbility.user === target)
  const targetOverlappedStuns = overlappedStuns.filter(overlappedStun => overlappedStun.user === target)
  const targetMidasEfficiency = midasEfficiency.filter(me => me.hero === target)
  const targetPTUsages = powerTreadsAbilityUsages.filter(usage => usage.heroId === target)

  const [abilities, setAbilities] = useState<ReadonlyArray<HeroAbility>>(null)
  const [selectedAbilityOrItemId, setSelectedAbilityOrItemId] = useState(null)

  useEffect(() => {
    fetch(Routes.Constants.getHeroAbilities(target))
      .then(response => response.json())
      .then(abilities => setAbilities(abilities))
  }, [])

  if (abilities === null)
    return null;

  const targetAbilities = abilities.filter(ability => ability.slot <= 6)
    .filter(ability => ability.id !== 6251)
    .filter(ability => !ability.isGrantedByShard || shardOwners.includes(target))
    .filter(ability => !ability.isGrantedByScepter || scepterOwners.includes(target))

  const onAbilityOrItemClick = (abilityOrItemId) => {
    if (selectedAbilityOrItemId === abilityOrItemId) {
      setSelectedAbilityOrItemId(null)
    }
    else {
      setSelectedAbilityOrItemId(abilityOrItemId)
    }
  }

  const abilitiesIcons = targetAbilities.map(ability => {
    const abilityClassName = ability.id === selectedAbilityOrItemId ? styles.selectedAbility : styles.ability
    return <div className={abilityClassName}>
      <button onClick={() => onAbilityOrItemClick(ability.id)}>
        <Image src={Routes.Images.getAbilityIcon(ability.id)} width={100} height={100}/>
      </button>
    </div>;
  })

  const selectedAbilityOrItem = targetAbilities.find(a => a.id === selectedAbilityOrItemId)
  const unusedSelectedAbility = targetUnusedAbilities.filter(unusedAbility => unusedAbility.ability == selectedAbilityOrItemId)
  const unusedSelectedAbilityOnSelf = unusedSelectedAbility.filter(ua => ua.user == ua.target)
  const unusedSelectedAbilityOnSelfTimes = unusedSelectedAbilityOnSelf.length === 0 ? [] : unusedSelectedAbilityOnSelf.map(ua =>
    <span className={styles.time}>{ua.time}</span>
  ).reduce((prev, curr) => [prev, ', ', curr])

  const unusedSelectedAbilitySpan = <span>{unusedSelectedAbilityOnSelf.length} times <span className={styles.insight}>not used</span> before <span className={styles.insight}>death</span> at {unusedSelectedAbilityOnSelfTimes}</span>

  return (
    <div>
      <span className={styles.title}>Abilities & Items usage</span>
      <div className={styles.hud}>
        <div className={styles.portrait}>
          <Image src={Routes.Images.getVerticalPortrait(target)} width={142} height={188} />
        </div>
        <div className={styles.abilities}>
          <HStack spacing='24px'>
            {abilitiesIcons}
          </HStack>
        </div>
        <div>

        </div>
      </div>
      {selectedAbilityOrItemId && <div className={styles.mistakes}>
        <span className={styles.mistakesTitle}>{selectedAbilityOrItem.displayName}</span>
        <div className={styles.mistakesEntries}>
          {unusedSelectedAbilityOnSelf.length > 0 && <>{unusedSelectedAbilitySpan}</>}
        </div>
      </div>}
    </div>
  )
}

export default Hud