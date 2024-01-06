import styles from "./DeathSummary.module.css";
import Image from "next/image";
import { Tooltip } from "@chakra-ui/tooltip";

interface GoldPenaltyProps {
  amount: number
}

const GoldPenalty = ({amount} : GoldPenaltyProps) => {
  return (
    <div>
      <Tooltip label={amount + ' gold total NW lost'} bg='black' color={'darkgrey'} borderRadius={'6px'}>
        <span className={styles.goldPenalty}>
          <div className={styles.goldIcon}><Image src={'/gold.png'} width={20} height={20} /></div>
          {amount}
        </span>
      </Tooltip>
    </div>
  )
}

export default GoldPenalty