import { HeroAbility } from "../../../models/HeroAbility";
import { UnusedAbility } from "../../../models/UnusedAbility";
import { OverlappedStun } from "../../../models/OverlappedStun";

import styles from "./Hud.module.css";
import MiniIcon from "../../MiniIcon";

interface UsageInfoProps {
  selectedAbilityId: number
  abilities: ReadonlyArray<HeroAbility>
  unusedAbilities: ReadonlyArray<UnusedAbility>
  overlappedStuns: ReadonlyArray<OverlappedStun>
}

const UsageInfo = ({ selectedAbilityId, abilities, unusedAbilities, overlappedStuns }: UsageInfoProps) => {
  if (!abilities)
    return null

  if (!selectedAbilityId)
    return null

  const selectedAbility = abilities.find(a => a.id === selectedAbilityId)
  const unusedSelectedAbility = unusedAbilities.filter(unusedAbility => unusedAbility.ability === selectedAbilityId)
  const overlappedAbilityStun = overlappedStuns.filter(stun => stun.isAbility && stun.sourceId == selectedAbilityId)

  const unusedSelectedAbilityOnSelf = unusedSelectedAbility.filter(ua => ua.user === ua.target)
  const unusedSelectedAbilityOnSelfTimes = unusedSelectedAbilityOnSelf.length === 0 ? null : unusedSelectedAbilityOnSelf.map(ua =>
    <span className={styles.time}>{ua.time}</span>
  ).reduce((prev, curr) => [prev, ', ', curr])

  const unusedSelectedAbilitySpan = <span>
    {unusedSelectedAbilityOnSelf.length} time{unusedSelectedAbilityOnSelf.length > 1 ? 's' : ''} <span className={styles.insight}>not used</span> before <span className={styles.insight}>death</span> at {unusedSelectedAbilityOnSelfTimes}
  </span>

  const unusedAbilityOnAllies = unusedSelectedAbility.filter(ua => ua.user !== ua.target)
  const unusedAbilityOnAlliesTimes = unusedAbilityOnAllies.length === 0 ? null : unusedAbilityOnAllies.map(ua => {
    let time = <span className={styles.time}>{ua.time}</span>
    let targetIcon = <MiniIcon heroId={ua.target} />
    return <span>on {targetIcon} at {time}</span>
  }).reduce((prev, curr) => [prev, ', ', curr])

  const unusedAbilityOnAlliesSpan = <span>
    {unusedAbilityOnAllies.length} times{unusedAbilityOnAllies.length > 1 ? 's' : ''} <span className={styles.insight}>not used</span> on a <span className={styles.insight}>dying ally</span> — {unusedAbilityOnAlliesTimes}
  </span>

  const overlappedStunTimes = overlappedAbilityStun.length === 0 ? null : overlappedAbilityStun.map(stun => {
    let time = <span className={styles.time}>{stun.time}</span>
    let targetIcon = <MiniIcon heroId={stun.target} />
    return <span>{targetIcon} at {time} ({stun.overlappedTime.toFixed(1)} seconds <span className={styles.insight}>overlap</span>)</span>
  }).reduce((prev, curr) => [prev, ', ', curr])

  const overlappedStunSpan = <span>
    {overlappedAbilityStun.length} time{overlappedAbilityStun.length > 1 ? 's' : ''} <span className={styles.insight}>stunned</span> an <span className={styles.insight}>already disabled</span> hero — {overlappedStunTimes}
  </span>

  return (
    <div className={styles.usages}>
      <span className={styles.usagesTitle}>{selectedAbility.displayName}</span>
      <div className={styles.usagesEntries}>
        {unusedSelectedAbilityOnSelf.length > 0 && <div>{unusedSelectedAbilitySpan}</div>}
        {unusedAbilityOnAllies.length > 0 && <div>{unusedAbilityOnAlliesSpan}</div>}
        {overlappedAbilityStun.length >0 && <div>{overlappedStunSpan}</div>}
      </div>
    </div>
  )
}

export default UsageInfo