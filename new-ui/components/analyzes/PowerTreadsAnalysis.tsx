import {PowerTreadsAbilityUsages} from "../../models/PowerTreadsAbilityUsages";
import styles from "../../styles/CommonAnalysis.module.css"
import { useDisclosure } from "@chakra-ui/hooks";
import { Collapse } from "@chakra-ui/transition";
import { Stack } from "@chakra-ui/layout";
import MiniIcon from "../MiniIcon";

interface PowerTreadsProps {
  powerTreadsAbilityUsages: ReadonlyArray<PowerTreadsAbilityUsages>
}

const PowerTreadsAnalysis = ({ powerTreadsAbilityUsages }: PowerTreadsProps) => {
  const { isOpen, onToggle } = useDisclosure({ defaultIsOpen: true })

  const entries = powerTreadsAbilityUsages.map(entry => {
    let hero = <MiniIcon heroId={entry.heroId} />
    let total = <span className={styles.bold}>{entry.total}</span>
    let onInt = <span className={styles.bold}>{entry.onInt}</span>
    let manaLost = <span className={styles.bold}>{Math.floor(entry.manaLost)}</span>
    let claritiesCount = <span className={styles.bold}>{(entry.manaLost / 150).toFixed(1)}</span>

    return <span className={styles.entry}>{hero} used {total} abilities while farming, only {onInt} with PT on Intelligence ({manaLost} mana lost = {claritiesCount} clarities)</span>
  })

  return (
    <div className={styles.container}>
      <button className={styles.title} onClick={onToggle}>Power Treads Toggling</button>
      <Collapse in={isOpen}>
        <div className={styles.entries}>
          <Stack>
            {entries}
          </Stack>
        </div>
      </Collapse>
    </div>
  )
}

export default PowerTreadsAnalysis