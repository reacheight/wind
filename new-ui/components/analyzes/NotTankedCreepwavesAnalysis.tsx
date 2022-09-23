import { NotTankedCreepwave } from "../../models/NotTankedCreepwave";
import styles from "../../styles/CommonAnalysis.module.css"
import MiniIcon from "../MiniIcon";
import { HStack, Stack } from "@chakra-ui/layout";
import { Collapse, useDisclosure } from "@chakra-ui/react";

interface NotTankedCreepwavesProps {
  notTankedCreepwaves: ReadonlyArray<NotTankedCreepwave>
}

const NotTankedCreepwavesAnalysis = ({ notTankedCreepwaves }: NotTankedCreepwavesProps) => {
  const { isOpen, onToggle } = useDisclosure({ defaultIsOpen: true})

  const entries = notTankedCreepwaves.map(entry => {
    let time = <span className={styles.time}>{entry.time}</span>
    let icons = <HStack>{entry.heroes.map(hero => <MiniIcon heroId={hero} />)}</HStack>

    return <div className={styles.entry}>{time} {icons} didn't tank creepwave</div>
  })

  return (
    <div className={styles.container}>
      <button className={styles.title} onClick={onToggle}>Not tanked creepwaves</button>
      <Collapse in={isOpen}>
        <div className={styles.entries}>
          <Stack>
            {entries}
          </Stack>
        </div>
      </Collapse>
    </div>
  )
}

export default NotTankedCreepwavesAnalysis