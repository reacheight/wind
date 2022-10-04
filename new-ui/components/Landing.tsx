import styles from "../styles/Landing.module.css";
import LoginButton from "./LoginButton";
import Image from "next/image";

const Landing = () =>
  <div>
    <main className={styles.main}>
      <h1 className={styles.title}>
        Dota 2 post match analysis
      </h1>
      <div className={styles.loginButton}>
        <LoginButton/>
      </div>
      <div className={styles.example}>
        <Image src={"/example.png"} width={1601} height={925} />
      </div>
    </main>
  </div>

export default Landing