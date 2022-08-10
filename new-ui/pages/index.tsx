import styles from '../styles/Home.module.css'
import { getUserContext } from "../components/UserContextWrapper";
import LoginButton from "../components/LoginButton";

const Home = () => {
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
          <div className={styles.loginButton}>
            <LoginButton />
          </div>
        }
      </main>
    </div>
  )
}

export default Home