import React from "react"
import styles from "./Resources.module.css";
import Image from "next/image";

interface SummonGoldFedProps {
  goldFed: number
}

const SummonGoldFedAnalysis = ({ goldFed }: SummonGoldFedProps) => (
  <div className={styles.container}>
    <div className={styles.summons}><Image src={"/summons.png"} width={124} height={96} /></div>
    <span className={styles.text}>
      You <span className={styles.insight}>fed {goldFed} gold</span> to enemies with your <span className={styles.insight}>summons</span>
    </span>
  </div>
)

export default SummonGoldFedAnalysis