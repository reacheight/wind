import { DeathSummary } from "../../../models/DeathSummary";
import { PlayerHero } from "../../../models/PlayerHero";
import Routes from "../../../api/routs";
import Image from "next/image";
import styles from "./DeathSummary.module.css"
import { VStack } from "@chakra-ui/react";
import { DamageBreakdownEntry } from "./DamageBreakdownEntry";
import { HeroAbilities } from "../../../models/HeroAbility";
import { Item } from "../../../models/Item";

interface DeathSummaryExpandedProps {
  deathSummaryEntry: DeathSummary
  playersHeroes: ReadonlyArray<PlayerHero>
  abilities: HeroAbilities[]
  items: Item[]
}

const DeathSummaryExpanded = ({ deathSummaryEntry, playersHeroes, abilities, items }: DeathSummaryExpandedProps) => {
  const damageBreakdown = deathSummaryEntry.damageReceived.map(dr => {
    let abilityDamage = dr.abilityDamage.map(ad =>
      <DamageBreakdownEntry
        iconSource={Routes.Images.getAbilityIcon(ad.abilityId)}
        sourceName={abilities.flatMap(ha => ha.abilities).find(ha => ha.id === ad.abilityId).displayName}
        damageAmount={ad.damage} />
    )
    let itemDamage = dr.itemDamage.map(id =>
      <DamageBreakdownEntry
        iconSource={Routes.Images.getItemIcon(id.ItemId)}
        sourceName={items.find(item => item.id === id.ItemId).displayName}
        damageAmount={id.damage}
        isItem />
    )

    return (
      <div className={styles.damageBreakdown}>
        <div className={styles.portrait}><Image src={Routes.Images.getHorizontalPortrait(dr.from)} layout={'fill'} objectFit={'contain'}/></div>
        <DamageBreakdownEntry iconSource={'/attack.webp'} sourceName={'attack'} damageAmount={dr.attackDamage} />
        {abilityDamage}
        {itemDamage}
      </div>
    )
  })

  return (
    <div className={styles.expandedContainer}>
      <VStack align={'left'}>
        {damageBreakdown}
      </VStack>
    </div>
  )
}

export default DeathSummaryExpanded