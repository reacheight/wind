import Image from "next/image";
import { getUserContext } from "./UserContextWrapper";
import LoginButton from "./LoginButton";

const Miniprofile = () => {
  let userContext = getUserContext()

  if (userContext.loading)
    return null

  if (userContext.user === null)
    return <LoginButton />

  return <Image src={userContext.user.avatarUrl} width={50} height={50}></Image>
}

export default Miniprofile