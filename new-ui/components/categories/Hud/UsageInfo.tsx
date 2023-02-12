import { HeroAbility } from "../../../models/HeroAbility";
import { UnusedAbility } from "../../../models/UnusedAbility";
import { OverlappedStun } from "../../../models/OverlappedStun";

import styles from "./Hud.module.css";

interface UsageInfoProps {
  selectedAbilityId: number
  abilities: ReadonlyArray<HeroAbility>
  unusedAbilities: ReadonlyArray<UnusedAbility>
  overlappedStuns: ReadonlyArray<OverlappedStun>
}

const UsageInfo = ({ selectedAbilityId, abilities, unusedAbilities, overlappedStuns }: UsageInfoProps) => {
  if (!selectedAbilityId)
    return null

  const selectedAbility = abilities.find(a => a.id === selectedAbilityId)
  const unusedSelectedAbility = unusedAbilities.filter(unusedAbility => unusedAbility.ability === selectedAbilityId)
  const unusedSelectedAbilityOnSelf = unusedSelectedAbility.filter(ua => ua.user === ua.target)
  const unusedSelectedAbilityOnSelfTimes = unusedSelectedAbilityOnSelf.length === 0 ? [] : unusedSelectedAbilityOnSelf.map(ua =>
    <span className={styles.time}>{ua.time}</span>
  ).reduce((prev, curr) => [prev, ', ', curr])

  const unusedSelectedAbilitySpan = <span>
    {unusedSelectedAbilityOnSelf.length} times <span className={styles.insight}>not used</span> before <span className={styles.insight}>death</span> at {unusedSelectedAbilityOnSelfTimes}
  </span>

  return (
    <div className={styles.mistakes}>
      <span className={styles.mistakesTitle}>{selectedAbility.displayName}</span>
      <div className={styles.mistakesEntries}>
        {unusedSelectedAbilityOnSelf.length > 0 && <>{unusedSelectedAbilitySpan}</>}
      </div>
    </div>
  )
}

export default UsageInfo