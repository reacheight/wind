import Image from "next/image";
import { getUserContext } from "./UserContextWrapper";
import LoginButton from "./LoginButton";
import { Menu, MenuButton, MenuItem, MenuList } from "@chakra-ui/menu";
import Routes from "../api/routs";

const Miniprofile = () => {
  let userContext = getUserContext()

  if (userContext.loading)
    return null

  if (userContext.user === null)
    return <LoginButton />

  return (
    <Menu autoSelect={false}>
      <MenuButton>
        <Image src={userContext.user.avatarUrl} width={50} height={50}></Image>
      </MenuButton>
      <MenuList border={'none'} bg={"whiteAlpha.200"}>
        <a href={Routes.Account.logout}><MenuItem fontWeight={'600'}>Log Out</MenuItem></a>
      </MenuList>
    </Menu>
  )
}

export default Miniprofile