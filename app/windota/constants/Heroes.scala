package windota.constants

import io.circe.generic.auto._
import io.circe.parser._

import scala.io.Source

// https://github.com/SteamDatabase/SteamTracking/blob/master/API - for actual api
// https://api.steampowered.com/IEconDOTA2_570/GetHeroes/v0001/?key=APIKEY
object Heroes {
  private val rawJson = Source.fromResource("heroes.json").mkString
  private val json = parse(rawJson)
  private val result = json.right.get.as[HeroesJson].right.get.result

  private val idToName = result.heroes.map(hero => (hero.id, hero.name)).toMap
  private val nameToId = idToName.map(_.swap)

  def getName(id: Int): String = idToName(id)
  def getTag(id: Int): String = getName(id).replace("npc_dota_hero_", "")
  def getId(name: String): Int = nameToId(name)
}

case class HeroJson(name: String, id: Int)
case class HeroesJsonResult(heroes: List[HeroJson], status: Int, count: Int)
case class HeroesJson(result: HeroesJsonResult)
