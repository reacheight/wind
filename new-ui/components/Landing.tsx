import styles from "../styles/Landing.module.css";
import LoginButton from "./LoginButton";

const Landing = () =>
  <div>
    <main className={styles.main}>
      <h1 className={styles.title}>
        Dota 2 post match analysis
      </h1>
      <div className={styles.loginButton}>
        <LoginButton/>
      </div>
    </main>
  </div>

export default Landing