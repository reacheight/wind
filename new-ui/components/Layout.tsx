import styles from '../styles/Layout.module.css'
import Logo from "./Logo";

const Layout = ({ children }) =>
  <div className={styles.container}>
    <Logo />
    {children}
  </div>

export default Layout