import Image from "next/image";
import { getUserContext } from "./UserContextWrapper";
import styles from '../styles/Profile.module.css'
import MatchesList from "./MatchesList";
import { Box } from "@chakra-ui/layout";

const Profile = () => {
  let userContext = getUserContext()

  if (userContext.loading || userContext.user === null)
    return null

  return (
    <div>
      <div className={styles.profile}>
        <Image src={userContext.user.avatarUrl} width={100} height={100}></Image>
        <span className={styles.name}>{userContext.user.name}</span>
      </div>

      <Box padding={"4rem 0"}><MatchesList /></Box>
    </div>
  )
}

export default Profile