import MatchPlayer from "../models/MatchPlayer";
import Image from "next/image";
import styles from "../styles/HeroMatchPreview.module.css";
import Routes from "../api/routs";
import Kda from "./Kda";
import { useRouter } from "next/router";

interface HeroMatchPreviewProps {
  player: MatchPlayer
}

const HeroMatchPreview = ({ player }: HeroMatchPreviewProps) => {
  const router = useRouter()
  const onHeroClick = () => {
    router.query.fromHeroView = player.heroId
    router.push(router)
  }

  return (
    <button onClick={() => onHeroClick()}>
      <div className={styles.main}>
        <div className={styles.portrait}>
          <Image src={Routes.Images.getHorizontalPortrait(player.heroId)} layout={'fill'} objectFit={'contain'}/>
        </div>
        <div className={styles.kda}>
          <Kda player={player}/>
        </div>
      </div>
    </button>
  )
}

export default HeroMatchPreview