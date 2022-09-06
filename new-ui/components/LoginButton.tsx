import Routes from "../api/routs";
import { Link } from "@chakra-ui/react";
import { Button } from "@chakra-ui/button";

const LoginButton = () => <Link href={Routes.Account.login}><Button>Log in with Steam</Button></Link>

export default LoginButton