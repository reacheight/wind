import styles from '../styles/Layout.module.css'
import Header from "./Header";
import Footer from "./Footer";

const Layout = ({ children }) =>
  <div className={styles.container}>
    <Header />
    <div className={styles.body}>
      {children}
    </div>
    <Footer />
  </div>

export default Layout