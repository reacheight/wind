export function formatHeroName(heroName) {
  return heroName.replace("_", " ").replace(/([A-Z])/g, " $1").slice(1)
}

export function isEmpty(obj) {
  return !obj || Object.keys(obj).length === 0
}