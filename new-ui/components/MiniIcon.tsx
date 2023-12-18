import Routes from "../api/routs";
import Image from "next/image";
import styles from "../styles/MiniIcon.module.css"

interface MiniIconProps {
  heroId: number
  width?: number
  height?: number
}

const MiniIcon = ({ heroId, width, height }: MiniIconProps) =>
  <span className={styles.icon}><Image src={Routes.Images.getIcon(heroId)} width={width ?? 30} height={height ?? 30} /></span>

export default MiniIcon