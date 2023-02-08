const HOST = `${process.env.NEXT_PUBLIC_BACKEND_HOST}`

class Account {
  login = `${HOST}/login`
  logout = `${HOST}/logout`
  user = `${HOST}/user`
}

class Analysis {
  start = (matchId: string) => `${HOST}/analysis/${matchId}`
  get = (matchId: string) => `${HOST}/analysis/${matchId}`
  state = (matchId: string) => `${HOST}/analysis/${matchId}/state`
}

class Matches {
  get = (matchId: number) => `${HOST}/matches/${matchId}`
}

class Players {
  getMatches = (accountId: number) => `${HOST}/players/${accountId}/matches`
}

class Images {
  getHorizontalPortrait = (heroId: number) => `${HOST}/miniportraits/${heroId}`

  getVerticalPortrait = (heroId: number) => `${HOST}/images/portraits/${heroId}`

  getIcon = (heroId: number) => `${HOST}/icons/${heroId}`

  getItemIcon = (itemId: number) => `${HOST}/items/${itemId}`

  getAbilityIcon = (abilityId: number) => `${HOST}/images/abilities/${abilityId}`
}

export default class Routes {
  public static Account = new Account()
  public static Analysis = new Analysis()
  public static Matches = new Matches()
  public static Players = new Players()
  public static Images = new Images()
}