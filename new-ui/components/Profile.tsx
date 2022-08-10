import Image from "next/image";
import { getUserContext } from "./UserContextWrapper";
import styles from '../styles/Profile.module.css'

const Profile = () => {
  let userContext = getUserContext()

  if (userContext.loading || userContext.user === null)
    return null

  return <div className={styles.profile}>
    <Image src={userContext.user.avatarUrl} width={100} height={100}></Image>
    <span className={styles.name}>{userContext.user.name}</span>
  </div>
}

export default Profile