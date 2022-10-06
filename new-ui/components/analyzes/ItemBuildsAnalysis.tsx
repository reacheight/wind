import {NotPurchasedStick} from "../../models/NotPurchasedStick";
import styles from "../../styles/CommonAnalysis.module.css"
import { useDisclosure } from "@chakra-ui/hooks";
import { Collapse } from "@chakra-ui/transition";
import { Stack } from "@chakra-ui/layout";
import MiniIcon from "../MiniIcon";

interface ItemBuildsProps {
  notPurchasedSticks: ReadonlyArray<NotPurchasedStick>
}

const ItemBuildsAnalysis = ({ notPurchasedSticks }: ItemBuildsProps) => {
  const { isOpen, onToggle } = useDisclosure({ defaultIsOpen: true})

  const entries = notPurchasedSticks.map(entry => {
    let hero = <MiniIcon heroId={entry.heroId} />
    let stickHero = <MiniIcon heroId={entry.stickHeroId} />

    return <div className={styles.entry}>{hero} didn't purchase <span className={styles.item}>Stick</span> versus {stickHero}</div>
  })

  return (
    <div className={styles.container}>
      <button className={styles.title} onClick={onToggle}>Item builds</button>
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

export default ItemBuildsAnalysis