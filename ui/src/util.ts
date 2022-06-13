export function getItemClassName(name: string) {
  return name.toLowerCase().replace(/ /g, '-')
}

// https://api.steampowered.com/IEconDOTA2_570/GetHeroes/v0001/?key=APIKEY
// https://api.steampowered.com/IEconDOTA2_570/GetGameItems/V001/?key=APIKEY