import { DamageAmount } from "../../../models/DamageAmount";
import styles from "./DeathSummary.module.css";
import Image from "next/image";
import { Tooltip } from "@chakra-ui/tooltip";

interface DamageBreakdownEntryProps {
  iconSource: string
  sourceName: string
  damageAmount: DamageAmount
  isItem?: boolean
}

export const DamageBreakdownEntry = ({ iconSource, sourceName, damageAmount, isItem }: DamageBreakdownEntryProps) => {
  const iconWidth = isItem ? 40 : 30
  return (
    <div className={styles.damageBreakdownEntries}>
      {damageAmount.physical > 0 && (
        <Tooltip label={damageAmount.physical + ' physical damage from ' + sourceName} bg='black' color={'darkgrey'} borderRadius={'6px'}>
          <span className={styles.damageBreakdownEntry}>
            <div className={styles.damageBreakdownEntryIcon} style={{width: iconWidth, height: '30px'}}><Image src={iconSource} width={iconWidth} height={30} /></div>
            <span className={styles.physical}>{damageAmount.physical}</span>
          </span>
        </Tooltip>
      )}
      {damageAmount.magical > 0 && (
        <Tooltip label={damageAmount.magical + ' magical damage from ' + sourceName} bg='black' color={'darkgrey'} borderRadius={'6px'}>
          <span className={styles.damageBreakdownEntry}>
            <div className={styles.damageBreakdownEntryIcon} style={{width: iconWidth, height: '30px'}}><Image src={iconSource} width={iconWidth} height={30} /></div>
            <span className={styles.magical}>{damageAmount.magical}</span>
          </span>
        </Tooltip>
      )}
      {damageAmount.pure > 0 && (
        <Tooltip label={damageAmount.pure + ' pure damage from ' + sourceName} bg='black' color={'darkgrey'} borderRadius={'6px'}>
          <span className={styles.damageBreakdownEntry}>
            <div className={styles.damageBreakdownEntryIcon} style={{width: iconWidth, height: '30px'}}><Image src={iconSource} width={iconWidth} height={30} /></div>
            <span className={styles.pure}>{damageAmount.pure}</span>
          </span>
        </Tooltip>
      )}
    </div>
  )
}