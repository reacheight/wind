import styles from "../styles/Landing.module.css";
import LoginButton from "./LoginButton";
import Image from "next/image";

const Landing = () =>
  <main className={styles.main}>
    <h1 className={styles.title}>
      Analyze your Dota 2 matches
    </h1>
    <div className={styles.description}>
      Get insights about what could be done better in your games with our <span className={styles.beta}>[super early β]</span> replay analysis assistant
    </div>
    <div className={styles.loginButton}>
      <LoginButton/>
    </div>
    <div className={styles.example}>
      <Image src={"/example.png"} width={1500} height={935} />
    </div>
  </main>

export default Landing