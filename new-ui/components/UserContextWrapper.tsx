import { createContext, useContext, useEffect, useState } from "react";
import User from "../models/User";
import AccountRoutes from "../api/account";

interface UserContext {
  loading: boolean
  user: User
}

const UserContext = createContext<UserContext>(null)

const UserContextWrapper = ({ children }) => {
  const [user, setUser] = useState<User>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetch(AccountRoutes.user, { credentials: 'include' })
      .then(response => response.json())
      .then(json => setUser(json))
      .catch(() => setUser(null))
      .finally(() => setLoading(false))
  }, [])

  return (
    <UserContext.Provider value={{ loading: loading, user: user }}>
      {children}
    </UserContext.Provider>
  )
}

export const getUserContext = () => useContext(UserContext);

export default UserContextWrapper