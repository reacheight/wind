import Image from "next/image";
import { getUserContext } from "./UserContextWrapper";
import LoginButton from "./LoginButton";
import Routes from "../api/routs";
import Link from "next/link";
import { CiLogout } from "react-icons/all";

import styles from '../styles/Miniprofile.module.css'

const Miniprofile = () => {
  let userContext = getUserContext()

  if (userContext.loading)
    return null

  if (userContext.user === null)
    return <LoginButton />

  return (
    <div className={styles.miniprofile}>
      <Link href={"/"}><a><Image src={userContext.user.avatarUrl} width={50} height={50}></Image></a></Link>
      <a className={styles.logout} href={Routes.Account.logout}><CiLogout size={'30px'} /></a>
    </div>
  )
}

export default Miniprofile