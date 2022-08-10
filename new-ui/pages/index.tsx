import { getUserContext } from "../components/UserContextWrapper";
import Profile from "../components/Profile";
import Landing from "../components/Landing";

const Home = () => {
  let userContext = getUserContext()

  if (userContext.loading)
    return null

  return (
    <div>
      {userContext.user === null && <Landing />}
      {userContext.user !== null && <Profile />}
    </div>
  )
}

export default Home