import styles from "./DeathSummary.module.css";
import { Tooltip } from "@chakra-ui/tooltip";
import Image from "next/image";

interface RespawnTimeProps {
  time: number
}

const RespawnTime = ({time} : RespawnTimeProps) => {
  return (
    <div>
      <Tooltip label={'respawn time'} bg='black' color={'darkgrey'} borderRadius={'6px'}>
        <span className={styles.respawnTime}>
          <div className={styles.clockIcon}><Image src={'/clock.svg'} width={17} height={17} /></div>
          {time}s
        </span>
      </Tooltip>
    </div>
  )
}

export default RespawnTime