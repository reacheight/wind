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
import { Item } from "../../../models/Item";
import ItemsPanel from "./ItemsPanel";

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
  const targetUnusedAbilities = unusedAbilities.filter(unusedAbility => unusedAbility.user === target)
  const targetOverlappedStuns = overlappedStuns.filter(overlappedStun => overlappedStun.user === target)

  const targetUnusedItems = unusedItems.filter(unusedItem => unusedItem.user === target)
  const targetMidasEfficiency = midasEfficiency.find(me => me.hero === target)
  const targetPTUsages = powerTreadsAbilityUsages.find(usage => usage.heroId === target)

  const targetActiveAbilities = targetUnusedAbilities.map(a => a.ability).concat(targetOverlappedStuns.filter(s => s.isAbility).map(s => s.sourceId))
  const targetActiveItems = [...new Set(targetUnusedItems.map(ui => ui.item))]

  if (targetMidasEfficiency)
    targetActiveItems.push(65)

  if (targetPTUsages)
    targetActiveItems.push(63)

  const [abilities, setAbilities] = useState<ReadonlyArray<HeroAbility>>(null)
  const [selectedAbilityId, setSelectedAbilityId] = useState(null)

  const [items, setItems] = useState<ReadonlyArray<Item>>(null)
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
    setAbilities(null)
    setSelectedAbilityId(null)
    setItems(null)
    setSelectedItemId(null)

    fetch(Routes.Constants.getHeroAbilities(target))
      .then(response => response.json())
      .then(abilities => setAbilities(abilities))

    fetch(Routes.Constants.getItems(targetActiveItems))
      .then(response => response.json())
      .then(items => setItems(items))
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
      <UsageInfo selectedAbilityId={selectedAbilityId} abilities={targetAbilities} unusedAbilities={targetUnusedAbilities} overlappedStuns={targetOverlappedStuns} />
    </div>
  )
}

export default Hud