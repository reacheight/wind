import { BadFight } from "../../models/BadFight";
import styles from "../../styles/CommonAnalysis.module.css";
import { useDisclosure } from "@chakra-ui/hooks";
import { Collapse } from "@chakra-ui/transition";
import { Stack } from "@chakra-ui/layout";
import MiniIcon from "../MiniIcon";

interface BadMapSplitProps {
  badFights: ReadonlyArray<BadFight>;
}

const BadMapSplitAnalysis = ({ badFights }: BadMapSplitProps) => {
  const { isOpen, onToggle } = useDisclosure({ defaultIsOpen: true})

  const entries = badFights.map(fight => {
    let time = <span className={styles.time}>{fight.time}</span>
    let seenHeroesIcons = fight.seenHeroes.map(h => <MiniIcon heroId={h} />)

    return <div className={styles.entry}>
      {time}
      {seenHeroesIcons} showed up on enemy vision far from their teammates who got outnumbered in the fight
    </div>
  })

  return (
    <div className={styles.container}>
      <button className={styles.title} onClick={onToggle}>Bad map split</button>
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

export default BadMapSplitAnalysis