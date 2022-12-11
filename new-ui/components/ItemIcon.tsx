import Routes from "../api/routs";
import Image from "next/image";
import styles from "../styles/ItemIcon.module.css"

interface ItemIconProps {
  itemId: number
}

const ItemIcon = ({ itemId }: ItemIconProps) =>
  <div className={styles.icon}><Image src={Routes.Images.getItemIcon(itemId)} width={40} height={30} /></div>

export default ItemIcon