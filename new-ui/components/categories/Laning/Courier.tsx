import styles from './Laning.module.css'
import Image from "next/image";

const Courier = () => (
  <div className={styles.container}>
    <Image src={"/courier.webp"} width={120} height={120} />
    <span className={styles.text}>
      You didn't place your <span className={styles.insight}>courier out of the fountain</span> at the start of the game
    </span>
  </div>
)

export default Courier