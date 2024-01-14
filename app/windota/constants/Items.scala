package windota.constants

import io.circe.generic.auto._
import io.circe.parser._
import windota.models.ItemId

import scala.io.Source

// NEW JSON: https://www.dota2.com/datafeed/abilitylist?language=English

// https://github.com/SteamDatabase/SteamTracking/blob/master/API - for actual api
// https://api.steampowered.com/IEconDOTA2_570/GetGameItems/V001/?key=APIKEY
object Items {
  private val rawJson = Source.fromResource("items.json").mkString
  private val json = parse(rawJson)
  private val items = json.right.get.as[ItemsJson].right.get.result.data.itemabilities

  private val idToName = items.map(item => (item.id, item.name)).toMap
  private val idToDisplayName = items.map(item => (item.id, item.name_loc)).toMap
  private val nameToId = idToName.map(_.swap)

  def getName(id: Int): String = idToName(id)
  def getDisplayName(id: Int): String = idToDisplayName(id)
  def getTag(id: Int): String = getName(id).replace("item_", "")
  def getId(name: String): Int = nameToId(name)
  def findId(name: String): Option[ItemId] = nameToId.get(name).map(id => ItemId(id))
}

case class ItemJson(id: Int, name: String, name_loc: String, name_english_loc: String, neutral_item_tier: Int)
case class ItemJsonData(itemabilities: List[ItemJson])
case class ItemsJsonResult(data: ItemJsonData)
case class ItemsJson(result: ItemsJsonResult)
