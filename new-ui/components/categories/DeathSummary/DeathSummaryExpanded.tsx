import { DeathSummary } from "../../../models/DeathSummary";
import { PlayerHero } from "../../../models/PlayerHero";
import Routes from "../../../api/routs";
import Image from "next/image";
import styles from "./DeathSummary.module.css"
import { VStack } from "@chakra-ui/react";
import { DamageBreakdownEntry } from "./DamageBreakdownEntry";
import { HeroAbilities } from "../../../models/HeroAbility";
import { Item } from "../../../models/Item";
import { calculateFullDamageReceived } from "../../../utils";
import { UnusedItem } from "../../../models/UnusedItem";
import { UnusedAbility } from "../../../models/UnusedAbility";

interface DeathSummaryExpandedProps {
  deathSummaryEntry: DeathSummary
  abilities: HeroAbilities[]
  items: Item[]
  unusedItems: ReadonlyArray<UnusedItem>
  unusedAbilities: ReadonlyArray<UnusedAbility>
}

const DeathSummaryExpanded = ({ deathSummaryEntry, abilities, items, unusedItems, unusedAbilities }: DeathSummaryExpandedProps) => {
  const deathUnusedItems = unusedItems.filter(ui => ui.user === deathSummaryEntry.hero && ui.target === deathSummaryEntry.hero && ui.time === deathSummaryEntry.time)
  const deathUnusedAbilities = unusedAbilities.filter(ua => ua.user === deathSummaryEntry.hero && ua.target === deathSummaryEntry.hero && ua.time === deathSummaryEntry.time)

  const deathSummaryHeroEntries = deathSummaryEntry.damageReceived.map(dr => {
    let goldEarnings = deathSummaryEntry.goldEarnings.find(e => e.hero === dr.from)
    let totalDamage = calculateFullDamageReceived(dr)

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
      <div className={styles.deathSummaryHeroEntry}>
        <div className={styles.portrait}><Image src={Routes.Images.getHorizontalPortrait(dr.from)} layout={'fill'} objectFit={'contain'}/></div>
        <div className={styles.damageBreakdownFull}>
          <div className={styles.damageBreakdownShort}>
            <DamageBreakdownEntry iconSource={'/attack.webp'} sourceName={'attack'} damageAmount={dr.attackDamage} />
            {abilityDamage}
            {itemDamage}
          </div>
          <span className={styles.damageBreakdownTotal}>{totalDamage}</span>
        </div>
        <div className={styles.verticalLine} />
        {goldEarnings && (
          <div className={styles.goldEarning}>
            <div className={styles.goldIcon}><Image src={'/gold.png'} width={20} height={20} /></div>
            +{goldEarnings.amount}
          </div>
        )}
      </div>
    )
  })

  return (
    <div className={styles.expandedContainer}>
      <VStack align={'left'}>
        {deathSummaryHeroEntries}
      </VStack>
      {(deathUnusedAbilities.length > 0 || deathUnusedItems.length > 0) && (
        <div className={styles.unusedItemsAndAbilities}>
          <span className={styles.miniTitle}>Unused items and abilities</span>
          <div className={styles.unusedItemsAndAbilitiesList}>
            {deathUnusedAbilities.map(ua => <div className={styles.unusedItemOrAbilityIcon}><Image src={Routes.Images.getAbilityIcon(ua.ability)} width={30} height={30} /></div>)}
            {deathUnusedItems.map(ui => <div className={styles.unusedItemOrAbilityIcon}><Image src={Routes.Images.getItemIcon(ui.item)} width={40} height={30} /></div>)}
          </div>
        </div>
      )}
    </div>
  )
}

export default DeathSummaryExpanded