export function formatHeroName(heroName) {
  return heroName.replace("_", " ").replace(/([A-Z])/g, " $1").slice(1);
}