import Image from "next/image";
import Routes from "../../../api/routs";
import { HStack } from "@chakra-ui/layout";

import styles from "./Hud.module.css";
import { Item } from "../../../models/Item";

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

  return (
    <div className={styles.abilities}>
      <HStack spacing='20px'>
        {itemsIcons}
      </HStack>
    </div>
  )
}

export default AbilitiesPanel