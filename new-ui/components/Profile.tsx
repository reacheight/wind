import Image from "next/image";
import { getUserContext } from "./UserContextWrapper";
import styles from '../styles/Profile.module.css'
import Matches from "./Matches";
import { Box } from "@chakra-ui/layout";

const Profile = () => {
  let userContext = getUserContext()

  if (userContext.loading || userContext.user === null)
    return null

  return (
    <div>
      {userContext.user.isAnonymous && <span>Enable "Expose Public Match Data" setting in the Dota 2 game client.</span>}
      <div className={styles.profile}>
        <Image src={userContext.user.avatarUrl} width={100} height={100}></Image>
        <span className={styles.name}>{userContext.user.name}</span>
      </div>

      {!userContext.user.isAnonymous && <Box padding={"4rem 0"}><Matches /></Box>}
    </div>
  )
}

export default Profile