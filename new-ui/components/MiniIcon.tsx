import Routes from "../api/routs";
import Image from "next/image";
import styles from "../styles/MiniIcon.module.css"

interface MiniIconProps {
  heroId: number
}

const MiniIcon = ({ heroId }: MiniIconProps) =>
  <span className={styles.icon}><Image src={Routes.Images.getIcon(heroId)} width={30} height={30} /></span>

export default MiniIcon