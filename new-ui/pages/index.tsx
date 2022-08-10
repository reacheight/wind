import { Button } from "@chakra-ui/button";
import { Link } from "@chakra-ui/react";
import styles from '../styles/Home.module.css'
import { getUserContext } from "../components/UserContextWrapper";

const Home = () => {
  const loginLink = `${process.env.NEXT_PUBLIC_BACKEND_HOST}/login`
  let userContext = getUserContext()

  if (userContext.loading)
    return null

  return (
    <div>
      <main className={styles.main}>
        <h1 className={styles.title}>
          Dota 2 post match analysis
        </h1>
        {userContext.user === null &&
          <p className={styles.description}>
            <Link href={loginLink}><Button>Log in with Steam</Button></Link>
          </p>
        }
      </main>
    </div>
  )
}

export default Home