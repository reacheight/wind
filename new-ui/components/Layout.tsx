import styles from '../styles/Layout.module.css'
import Header from "./Header";

const Layout = ({ children }) =>
  <div className={styles.container}>
    <Header />
    <div className={styles.body}>
      {children}
    </div>
  </div>

export default Layout