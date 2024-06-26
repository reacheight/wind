import { UnusedItem } from "../../../models/UnusedItem";
import { UnusedAbility } from "../../../models/UnusedAbility";
import { OverlappedStun } from "../../../models/OverlappedStun";
import { MidasEfficiency } from "../../../models/MidasEfficiency";
import { PowerTreadsAbilityUsages } from "../../../models/PowerTreadsAbilityUsages";
import { HeroId } from "../../../models/HeroId";
import Image from "next/image";
import Routes from "../../../api/routs";
import { useEffect, useState } from "react";
import { HeroAbilities, HeroAbility } from "../../../models/HeroAbility";
import AbilitiesPanel from "./AbilitiesPanel";
import AbilitiesUsageInfo from "./AbilitiesUsageInfo";

import styles from "./Hud.module.css"
import { Item } from "../../../models/Item";
import ItemsPanel from "./ItemsPanel";
import ItemsUsageInfo from "./ItemsUsageInfo";

interface HudProps {
  target: HeroId
  unusedItems: ReadonlyArray<UnusedItem>
  unusedAbilities: ReadonlyArray<UnusedAbility>
  overlappedStuns: ReadonlyArray<OverlappedStun>
  midasEfficiency: ReadonlyArray<MidasEfficiency>
  powerTreadsAbilityUsages: ReadonlyArray<PowerTreadsAbilityUsages>
  shardOwners: ReadonlyArray<HeroId>
  scepterOwners: ReadonlyArray<HeroId>
  allAbilities: HeroAbilities[]
  allItems: Item[]
}

const Hud = ({ target, unusedItems, unusedAbilities, overlappedStuns, midasEfficiency, powerTreadsAbilityUsages, scepterOwners, shardOwners, allAbilities, allItems }: HudProps) => {
  const targetUnusedAbilities = unusedAbilities.filter(unusedAbility => unusedAbility.user === target)
  const targetOverlappedStuns = overlappedStuns.filter(overlappedStun => overlappedStun.user === target)

  const targetUnusedItems = unusedItems.filter(unusedItem => unusedItem.user === target)
  const targetMidasEfficiency = midasEfficiency.find(me => me.hero === target)
  const targetPTUsages = powerTreadsAbilityUsages.find(usage => usage.heroId === target)

  const targetActiveAbilities = targetUnusedAbilities.map(a => a.ability).concat(targetOverlappedStuns.filter(s => s.isAbility).map(s => s.sourceId))
  const targetActiveItems = [...new Set(targetUnusedItems.map(ui => ui.item))]

  // if (targetMidasEfficiency)
  //   targetActiveItems.push(65)

  if (targetPTUsages)
    targetActiveItems.push(63)

  const abilities = allAbilities.find(x => x.heroId === target).abilities

  const [selectedAbilityId, setSelectedAbilityId] = useState(null)

  const items = allItems ? allItems.filter(item => targetActiveItems.includes(item.id)) : [];
  const [selectedItemId, setSelectedItemId] = useState(null)

  const onAbilityClick = (abilityId) => {
    setSelectedItemId(null)
    if (selectedAbilityId === abilityId) {
      setSelectedAbilityId(null)
    }
    else {
      setSelectedAbilityId(abilityId)
    }
  }

  const onItemClick = (itemId) => {
    setSelectedAbilityId(null)
    if (selectedItemId === itemId) {
      setSelectedItemId(null)
    } else {
      setSelectedItemId(itemId)
    }
  }

  useEffect(() => {
    setSelectedAbilityId(null)
    setSelectedItemId(null)
  }, [target])

  if (abilities === null)
    return null

  if (items === null)
    return null

  const targetAbilities = abilities.filter(ability => ability.slot <= 6)
    .filter(ability => ability.id !== 6251) // just default empty
    .filter(ability => ability.id !== 5453 && ability.id !== 5454) // rubick's stolen abilities
    .filter(ability => ability.id !== 5343 && ability.id !== 5344) // dooms's devour abilities
    .filter(ability => !ability.isGrantedByShard || shardOwners.includes(target))
    .filter(ability => !ability.isGrantedByScepter || scepterOwners.includes(target))

  return (
    <div>
      <span className={styles.title}>Abilities & Items usage</span>
      <div className={styles.hud}>
        <div className={styles.portrait}>
          <Image src={Routes.Images.getVerticalPortrait(target)} width={142} height={188} />
        </div>
        <AbilitiesPanel selectedAbility={selectedAbilityId} onAbilityClick={id => onAbilityClick(id)} abilities={targetAbilities} activeAbilities={targetActiveAbilities} />
        <ItemsPanel selectedItemId={selectedItemId} onItemClick={id => onItemClick(id)} items={items} />
      </div>
      <AbilitiesUsageInfo selectedAbilityId={selectedAbilityId} abilities={targetAbilities} unusedAbilities={targetUnusedAbilities} overlappedStuns={targetOverlappedStuns} />
      <ItemsUsageInfo selectedItemId={selectedItemId} items={items} unusedItems={targetUnusedItems} midasEfficiency={targetMidasEfficiency} powerTreadsAbilityUsages={targetPTUsages} />
    </div>
  )
}

export default Hud