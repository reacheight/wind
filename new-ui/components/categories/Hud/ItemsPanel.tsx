import Image from "next/image";
import Routes from "../../../api/routs";
import { Item } from "../../../models/Item";

import styles from "./Hud.module.css";

interface ItemsPanelProps {
  selectedItemId: number
  onItemClick: Function
  items: ReadonlyArray<Item>
}

const AbilitiesPanel = ({ selectedItemId, onItemClick, items }: ItemsPanelProps) => {
  if (!items)
    return null

  const itemsIcons = items.map(item => {
    const itemClassName = item.id === selectedItemId ? styles.selectedItem : styles.item
    return <div className={itemClassName}>
      <button onClick={() => onItemClick(item.id)}>
        <Image src={Routes.Images.getItemIcon(item.id)} width={66} height={48}/>
      </button>
    </div>;
  })

  const emptySlot = <div className={styles.emptyItemSlot}></div>
  const emptySlotsNumber = 6 - items.length
  const emptySlots = Array(emptySlotsNumber).fill(emptySlot)

  return (
    <div className={styles.itemsPanel}>
      {itemsIcons}
      {emptySlots}
    </div>
  )
}

export default AbilitiesPanel