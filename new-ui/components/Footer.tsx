import styles from "../styles/Footer.module.css"
import {FaDiscord, SiDiscord} from "react-icons/all";

const Footer = () => {
  return (
    <div className={styles.footer}>
      <div className={styles.icons}>
        <a href={"https://discord.gg/5pjkNzMRjC"}><SiDiscord size={'24px'} /></a>
      </div>
      <div>Dota 2 is a registered trademark of Valve Corporation.</div>
    </div>
  )
}

export default Footer