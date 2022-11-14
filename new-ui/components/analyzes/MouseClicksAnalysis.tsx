import { MouseClickItemDelivery } from "../../models/MouseClickItemDelivery";
import { MouseClickQuickBuy } from "../../models/MouseClickQuickBuy";
import styles from "../../styles/CommonAnalysis.module.css"
import mouseClicksStyles from "../../styles/MouseClicksAnalysis.module.css"
import { useDisclosure } from "@chakra-ui/hooks";
import { Collapse } from "@chakra-ui/transition";
import { Stack } from "@chakra-ui/layout";
import MiniIcon from "../MiniIcon";

interface MouseClicksPorps {
  mouseClickItemDeliveries: ReadonlyArray<MouseClickItemDelivery>;
  mouseClickQuickBuys: ReadonlyArray<MouseClickQuickBuy>;
}

const MouseClicksAnalysis = ({ mouseClickItemDeliveries, mouseClickQuickBuys }: MouseClicksPorps) => {
  const { isOpen, onToggle } = useDisclosure({ defaultIsOpen: true})

  const heroes = new Set<number>()

  const itemDeliveriesDict : { [id: number]: number } = {}
  mouseClickItemDeliveries.forEach(e => {
    itemDeliveriesDict[e.heroId] = e.count
    heroes.add(e.heroId)
  })

  const quickBuyPurchasesDict = {}
  mouseClickQuickBuys.forEach(e => {
    quickBuyPurchasesDict[e.heroId] = e.count
    heroes.add(e.heroId)
  })

  const entries = Array.from(heroes).map(hero => {
    let icon = <span className={mouseClicksStyles.icon}><MiniIcon heroId={hero} /></span>

    let itemDeliveries = itemDeliveriesDict[hero]
    let quickBuyPurchases = quickBuyPurchasesDict[hero]
    let itemDeliveriesPart = <><span className={mouseClicksStyles.count}>{itemDeliveries}</span> item deliveries&nbsp;&nbsp;&nbsp;&nbsp; {itemDeliveries < 10 && <>&nbsp;</>}</>
    let quickBuyPart = <><span className={mouseClicksStyles.count}>{quickBuyPurchases}</span> quickbuys</>

    return <span className={styles.entry}>{icon} {itemDeliveries > 0 && itemDeliveriesPart} {quickBuyPurchases > 0 && quickBuyPart}</span>
  })

  return (
    <div className={styles.container}>
      <button className={styles.title} onClick={onToggle}>Using mouse instead of hotkeys</button>
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

export default MouseClicksAnalysis