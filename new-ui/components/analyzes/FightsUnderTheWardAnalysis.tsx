import { LostFightsUnderTheSameWard } from "../../models/LostFightsUnderTheSameWard";
import styles from "../../styles/CommonAnalysis.module.css";
import { useDisclosure } from "@chakra-ui/hooks";
import { Collapse } from "@chakra-ui/transition";
import { Stack } from "@chakra-ui/layout";
import { Team } from "../../models/Team";
import MiniIcon from "../MiniIcon";

interface FightsUnderTheWardProps {
  fightsUnderTheWard: ReadonlyArray<LostFightsUnderTheSameWard>
}

const FightsUnderTheWardAnalysis = ({ fightsUnderTheWard }: FightsUnderTheWardProps) => {
  const { isOpen, onToggle } = useDisclosure({ defaultIsOpen: true})

  const entries = fightsUnderTheWard.map(entry => {
    let team = <span className={entry.loser == Team.Dire ? styles.dire : styles.radiant}>{Team[entry.loser]}</span>
    let times = <span className={styles.time}>{entry.fights.join(", ")}</span>
    let warderIcon = <MiniIcon heroId={entry.wardOwner} />

    return <span className={styles.entry}>{times} {team} lost {entry.fights.length} fights under the same ward by {warderIcon}</span>
  })

  return (
    <div className={styles.container}>
      <button className={styles.title} onClick={onToggle}>Fights lost under the same Observer</button>
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

export default FightsUnderTheWardAnalysis