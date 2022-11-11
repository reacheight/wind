import {NotPurchasedStick} from "../../models/NotPurchasedStick";
import styles from "../../styles/CommonAnalysis.module.css"
import { useDisclosure } from "@chakra-ui/hooks";
import { Collapse } from "@chakra-ui/transition";
import { Stack } from "@chakra-ui/layout";
import MiniIcon from "../MiniIcon";
import NotPurchasedItemAgainstHero from "../../models/NotPurchasedItemAgainstHero";
import {Tooltip} from "@chakra-ui/tooltip";
import { BsQuestionSquareFill } from "react-icons/all";

interface ItemBuildsProps {
  notPurchasedSticks: ReadonlyArray<NotPurchasedStick>
  notPurchasedItemAgainstHero: ReadonlyArray<NotPurchasedItemAgainstHero>
}

const ItemBuildsAnalysis = ({ notPurchasedSticks, notPurchasedItemAgainstHero }: ItemBuildsProps) => {
  const { isOpen, onToggle } = useDisclosure({ defaultIsOpen: true})

  const notPurchasedSticksEntries = notPurchasedSticks.map(entry => {
    let hero = <MiniIcon heroId={entry.heroId} />
    let stickHero = <MiniIcon heroId={entry.stickHeroId} />

    return <div className={styles.entry}>{hero} didn't purchase <span className={styles.bold}>Stick</span> versus {stickHero}</div>
  })

  const notPurchasedItemAgainstHeroEntries = notPurchasedItemAgainstHero.map(entry => {
    let hero = <MiniIcon heroId={entry.heroId} />
    let item = <span className={styles.bold}>{entry.itemName}</span>
    let candidates = entry.candidates.map(id => <MiniIcon heroId={id} />)
    let hint = <Tooltip placement='top' bg={"rgba(255, 255, 255)"} label={"Winrate difference with item is +" + (entry.itemWinrate - entry.noItemWinrate) + "%"}>
      <span><BsQuestionSquareFill /></span>
    </Tooltip>

    return <div className={styles.entry}>{candidates} didn't purchase {item} against {hero} {hint} </div>
  })

  return (
    <div className={styles.container}>
      <button className={styles.title} onClick={onToggle}>Item builds</button>
      <Collapse in={isOpen}>
        <div className={styles.entries}>
          <Stack>
            {notPurchasedSticksEntries}
            {notPurchasedItemAgainstHeroEntries}
          </Stack>
        </div>
      </Collapse>
    </div>
  )
}

export default ItemBuildsAnalysis