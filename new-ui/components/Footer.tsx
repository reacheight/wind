import styles from "../styles/Footer.module.css"
import Image from "next/image";

const Footer = () => {
  return (
    <div className={styles.footer}>
      <a className={styles.minilogo} href={"https://discord.gg"}><Image display={'block'} src="/discord.png" width={24} height={26} /></a>
      <div>Dota 2 is a registered trademark of Valve Corporation.</div>
    </div>
  )
}

export default Footer