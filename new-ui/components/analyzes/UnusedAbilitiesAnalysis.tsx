import { UnusedAbility } from "../../models/UnusedAbility";
import styles from "../../styles/CommonAnalysis.module.css";
import Routes from "../../api/routs";
import Image from "next/image";
import { Stack } from "@chakra-ui/layout";

interface UnusedAbilitiesAnalysisProps {
  unusedAbilities: ReadonlyArray<UnusedAbility>
}

const UnusedAbilitiesAnalysis = ({ unusedAbilities }: UnusedAbilitiesAnalysisProps) => {
  const getIcon = (heroId: number) => <span className={styles.heroIcon}><Image src={Routes.Images.getIcon(heroId)} width={30} height={30} /></span>

  const entries = unusedAbilities
    .map(entry => {
      let time = <span className={styles.time}>{entry.time}</span>
      let ability = <span className={styles.item}>{entry.ability}</span>
      let userIcon = getIcon(entry.user)
      let targetIcon = getIcon(entry.target)
      let isOnAlly = entry.user != entry.target
      let onTargetPart = <>on {targetIcon}</>

      return <div className={styles.entry}>{time} {userIcon} didn't use {ability} {isOnAlly && onTargetPart}</div>
    })

  return (
    <div className={styles.container}>
      <div className={styles.title}>Unused abilities</div>
      <div className={styles.entries}>
        <Stack>
          {entries}
        </Stack>
      </div>
    </div>
  )
}

export default UnusedAbilitiesAnalysis