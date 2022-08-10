import Logo from "./Logo";
import { getUserContext } from "./UserContextWrapper";
import Miniprofile from "./Miniprofile";
import styles from '../styles/Header.module.css'

const Header = () => {
  let userContext = getUserContext()

  if (userContext.loading)
    return null

  return (<>
    <Logo />
    <div className={styles.miniprofile}><Miniprofile /></div>
  </>)
}

export default Header