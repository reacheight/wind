import styles from '../styles/Layout.module.css'
import Header from "./Header";

const Layout = ({ children }) =>
  <div className={styles.container}>
    <Header />
    {children}
  </div>

export default Layout