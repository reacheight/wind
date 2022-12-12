package windota.constants

import io.circe.generic.auto._
import io.circe.parser._

import scala.io.Source

// https://api.steampowered.com/IEconDOTA2_570/GetGameItems/V001/?key=APIKEY
object Items {
  private val rawJson = Source.fromResource("items.json").mkString
  private val json = parse(rawJson)
  private val items = json.right.get.as[ItemsJson].right.get.result.items.filter(_.recipe == 0)

  private val idToName = items.map(item => (item.id, item.name)).toMap
  private val nameToId = idToName.map(_.swap)

  def getName(id: Int): String = idToName(id)

  def getTag(id: Int): String = getName(id).replace("item_", "")

  def getId(name: String): Int = nameToId(name)
}

case class ItemJson(id: Int, name: String, cost: Int, secret_shop: Int, side_shop: Int, recipe: Int)
case class ItemsJsonResult(items: List[ItemJson], status: Int)
case class ItemsJson(result: ItemsJsonResult)
