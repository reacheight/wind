const HOST = `${process.env.NEXT_PUBLIC_BACKEND_HOST}`

class Account {
  login = `${HOST}/login`
  logout = `${HOST}/logout`
  user = `${HOST}/user`
  matches = `${HOST}/user/matches`
}

class Analysis {
  start = (matchId: string) => `${HOST}/analysis/${matchId}`
  get = (matchId: string) => `${HOST}/analysis/${matchId}`
  state = (matchId: string) => `${HOST}/analysis/${matchId}/state`
}

export default class Routes {
  public static Account = new Account()
  public static Analysis = new Analysis()
}