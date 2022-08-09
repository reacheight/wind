import { Button } from "@chakra-ui/button";
import { Link } from "@chakra-ui/react";
import styles from '../styles/Home.module.css'

const Home = () => {
  let loginLink = `${process.env.NEXT_PUBLIC_BACKEND_HOST}/login`

  return (
    <div>
      <main className={styles.main}>
        <h1 className={styles.title}>
          Dota 2 post match analysis
        </h1>

        <p className={styles.description}>
          <Link href={loginLink}><Button>Log in with Steam</Button></Link>
        </p>
      </main>
    </div>
  )
}

export default Home