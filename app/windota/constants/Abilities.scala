package windota.constants

import io.circe.generic.auto._
import io.circe.parser._

import scala.io.Source

object Abilities {
  private val abilities = parse(Source.fromResource("abilities.json").mkString)
    .right.get.as[AbilitiesJson]
    .right.get.abilities

  private val idToName = abilities.map(item => (item.id, item.name)).toMap
  private val nameToId = idToName.map(_.swap)

  def getName(id: Int): String = idToName(id)
  def getId(name: String): Int = nameToId(name)
  def isAbilityName(name: String): Boolean = nameToId.contains(name)
}

case class AbilityJson(id: Int, name: String, isGrantedByShard: Boolean, isGrantedByScepter: Boolean, isUltimate: Boolean)
case class AbilitiesJson(abilities: List[AbilityJson])