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

interface DeathSummaryExpandedProps {
  deathSummaryEntry: DeathSummary
  abilities: HeroAbilities[]
  items: Item[]
}

const DeathSummaryExpanded = ({ deathSummaryEntry, abilities, items }: DeathSummaryExpandedProps) => {
  const damageBreakdown = deathSummaryEntry.damageReceived.map(dr => {
    let totalDamage = calculateFullDamageReceived(dr)
    let goldEarnings = deathSummaryEntry.goldEarnings.find(e => e.hero === dr.from)

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
        {damageBreakdown}
      </VStack>
    </div>
  )
}

export default DeathSummaryExpanded