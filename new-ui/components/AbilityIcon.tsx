import Routes from "../api/routs";
import Image from "next/image";
import styles from "../styles/ItemIcon.module.css"

interface AbilityIconProps {
  abilityId: number
}

const AbilityIcon = ({ abilityId }: AbilityIconProps) =>
  <div className={styles.icon}><Image src={Routes.Images.getAbilityIcon(abilityId)} width={30} height={30} /></div>

export default AbilityIcon