export function formatHeroName(heroName) {
  return heroName.replace("_", " ").replace(/([A-Z])/g, " $1").slice(1)
}

export function isEmpty(obj) {
  return !obj || Object.keys(obj).length === 0
}

export function compareTime(first, second) {
  let firstTokens = first.split(':').map((t) => parseInt(t))
  let secondTokens = second.split(':').map((t) => parseInt(t))

  if (firstTokens[0] == secondTokens[0]) {
    return firstTokens[1] - secondTokens[1]
  }

  return firstTokens[0] - secondTokens[0]
}

export function getItemClassName(name) {
  return name.toLowerCase().replace(" ", "-")
}