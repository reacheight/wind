import AccountRoutes from "../api/account";
import { Link } from "@chakra-ui/react";
import { Button } from "@chakra-ui/button";

const LoginButton = () => <Link href={AccountRoutes.login}><Button>Log in with Steam</Button></Link>

export default LoginButton