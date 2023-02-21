import styles from "./Hud.module.css";
import MiniIcon from "../../MiniIcon";
import { Item } from "../../../models/Item";
import { UnusedItem } from "../../../models/UnusedItem";
import { MidasEfficiency } from "../../../models/MidasEfficiency";
import { PowerTreadsAbilityUsages } from "../../../models/PowerTreadsAbilityUsages";

interface ItemsUsageInfoProps {
  selectedItemId: number
  items: ReadonlyArray<Item>
  unusedItems: ReadonlyArray<UnusedItem>
  midasEfficiency: MidasEfficiency
  powerTreadsAbilityUsages: PowerTreadsAbilityUsages
}

const AbilitiesUsageInfo = ({ selectedItemId, items, unusedItems, midasEfficiency, powerTreadsAbilityUsages }: ItemsUsageInfoProps) => {
  if (!items)
    return null

  if (!selectedItemId)
    return null

  const selectedItem = items.find(a => a.id === selectedItemId)
  if (!selectedItem)
    return null

  const unusedSelectedItem = unusedItems.filter(ui => ui.item === selectedItemId)
  const unusedSelectedItemOnSelf = unusedSelectedItem.filter(ui => ui.user === ui.target)
  const unusedSelectedItemOnAlly = unusedSelectedItem.filter(ui => ui.user !== ui.target)

  const unusedSelectedItemOnSelfTimes = unusedSelectedItemOnSelf.length === 0 ? null : unusedSelectedItemOnSelf.map(ua =>
    <span className={styles.time}>{ua.time}</span>
  ).reduce((prev, curr) => [prev, ', ', curr])

  const unusedSelectedItemOnSelfSpan = <span>
    {unusedSelectedItemOnSelf.length} time{unusedSelectedItemOnSelf.length > 1 ? 's' : ''} <span className={styles.insight}>not used</span> before <span className={styles.insight}>death</span> at {unusedSelectedItemOnSelfTimes}
  </span>

  const unusedItemOnAllyTimes = unusedSelectedItemOnAlly.length === 0 ? null : unusedSelectedItemOnAlly.map(ua => {
    let time = <span className={styles.time}>{ua.time}</span>
    let targetIcon = <MiniIcon heroId={ua.target} />
    return <span>on {targetIcon} at {time}</span>
  }).reduce((prev, curr) => [prev, ', ', curr])

  const unusedItemOnAlliesSpan = <span>
    {unusedSelectedItemOnAlly.length} time{unusedSelectedItemOnAlly.length > 1 ? 's' : ''} <span className={styles.insight}>not used</span> on a <span className={styles.insight}>dying ally</span> — {unusedItemOnAllyTimes}
  </span>

  return (
    <div className={styles.usages}>
      <span className={styles.usagesTitle}>{selectedItem.displayName}</span>
      <div className={styles.usagesEntries}>
        {unusedSelectedItemOnSelf.length > 0 && <div>{unusedSelectedItemOnSelfSpan}</div>}
        {unusedSelectedItemOnAlly.length > 0 && <div>{unusedItemOnAlliesSpan}</div>}
      </div>
    </div>
  )
}

export default AbilitiesUsageInfo