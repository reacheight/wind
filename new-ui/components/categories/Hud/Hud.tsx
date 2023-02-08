import { UnusedItem } from "../../../models/UnusedItem";
import { UnusedAbility } from "../../../models/UnusedAbility";
import { OverlappedStun } from "../../../models/OverlappedStun";
import { MidasEfficiency } from "../../../models/MidasEfficiency";
import { PowerTreadsAbilityUsages } from "../../../models/PowerTreadsAbilityUsages";
import { HeroId } from "../../../models/HeroId";

import styles from "./Hud.module.css"
import Image from "next/image";
import Routes from "../../../api/routs";
import { useEffect, useState } from "react";
import { HeroAbility } from "../../../models/HeroAbility";
import AbilityIcon from "../../AbilityIcon";
import { HStack, List, ListItem } from "@chakra-ui/layout";

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

  const abilitiesIcons = targetAbilities.map(ability =>
    <div className={styles.ability}>
      <Image src={Routes.Images.getAbilityIcon(ability.id)} width={100} height={100} />
    </div>
  )

  return (
    <div>
      <span className={styles.title}>Abilities & Items usage</span>
      <div className={styles.hud}>
        <div className={styles.portrait}>
          <Image src={Routes.Images.getVerticalPortrait(target)} width={142} height={188} />
        </div>
        <div className={styles.abilities}>
          <HStack>
            {abilitiesIcons}
          </HStack>
        </div>
      </div>
    </div>
  )
}

export default Hud