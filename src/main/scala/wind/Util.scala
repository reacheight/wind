package wind

import skadistats.clarity.model.Entity
import wind.models.{GameTimeState, Lane, Location}
import wind.models.Lane.Lane
import wind.models.Team._

import scala.collection.mutable.ArrayBuffer

object Util {
  val NullValue = 16777215
  private val TIME_EPS: Float = 0.001f
  private val replicatingPropertyName = "m_hReplicatingOtherHeroModel"
  private val glyphCooldownPropertyName: Map[Team, String] =
    Map(Radiant -> "m_pGameRules.m_fGoodGlyphCooldown", Dire -> "m_pGameRules.m_fBadGlyphCooldown")

  def getGameTimeState(gameRulesEntity: Entity): GameTimeState = {
    if (gameRulesEntity.getDtClass.getDtName != "CDOTAGamerulesProxy") throw new IllegalArgumentException

    val gameTime : Float = gameRulesEntity.getProperty("m_pGameRules.m_fGameTime")
    if (gameTime > TIME_EPS) {
      val preGameTime = gameRulesEntity.getProperty[Float]("m_pGameRules.m_flPreGameStartTime")

      if (preGameTime > TIME_EPS){
        val startTime = gameRulesEntity.getProperty[Float]("m_pGameRules.m_flGameStartTime")
        if (startTime > TIME_EPS) {
          return GameTimeState(true, true, gameTime - startTime)
        }
        else {
          val transitionTime = gameRulesEntity.getProperty[Float]("m_pGameRules.m_flStateTransitionTime")
          return GameTimeState(true, false, gameTime - transitionTime)
        }
      }

      return GameTimeState(false, false, Float.MinValue)
    }

    GameTimeState(false, false, Float.MinValue)
  }

  def isGlyphOnCooldown(gameRules: Entity, team: Team): Boolean =
    gameRules.getProperty[Float]("m_pGameRules.m_fGameTime") < gameRules.getProperty[Float](glyphCooldownPropertyName(team))

  def isHero(entity: Entity): Boolean =
    entity.getDtClass.getDtName.startsWith("CDOTA_Unit_Hero") &&
      entity.hasProperty(replicatingPropertyName) &&
      entity.getProperty[Int](replicatingPropertyName) == NullValue

  def isVisibleByEnemies(entity: Entity): Boolean = entity.getProperty[Int]("m_iTaggedAsVisibleByTeam") > 10

  def getLocation(entity: Entity): Location = {
    if (!entity.hasProperty("CBodyComponent.m_cellX") || !entity.hasProperty("CBodyComponent.m_cellY") ||
      !entity.hasProperty("CBodyComponent.m_vecX") || !entity.hasProperty("CBodyComponent.m_vecY")) {
      throw new IllegalArgumentException
    }

    val (x, y) = (entity.getProperty[Int]("CBodyComponent.m_cellX"), entity.getProperty[Int]("CBodyComponent.m_cellY"))
    val (vecX, vecY) = (entity.getProperty[Float]("CBodyComponent.m_vecX"), entity.getProperty[Float]("CBodyComponent.m_vecY"))

    Location(x * 128 + vecX - 8192, y * 128 + vecY - 8192)
  }

  def getAverageLocation(locations: Seq[Location]): Location = {
    val xs = locations.map(_.X)
    val ys = locations.map(_.Y)

    Location(xs.sum / xs.length, ys.sum / ys.length)
  }

  def getLane(l: Location): Lane = l match {
    case Location(x, y) if y > 10000 && x < 4500 => Lane.Top
    case Location(x, y) if y > 6000 && y < 10000 && x > 6000 && x < 10000 => Lane.Middle
    case Location(x, y) if y > 2000 && y < 6000 && x > 4000 && x < 11800 => Lane.RadiantJungle
    case Location(x, y) if y > 10000 && y < 14000 && x > 4500 && x < 12000 => Lane.DireJungle
    case Location(x, y) if y < 6000 && x > 11800 => Lane.Bot
    case _ => Lane.Roaming
  }

  def getDistance(first: Entity, second: Entity): Double = {
    val firstLocation = getLocation(first)
    val secondLocation = getLocation(second)

    getDistance(firstLocation, secondLocation)
  }

  def getDistance(first: Location, second: Location): Double = {
    val deltaX = math.pow(first.X - second.X, 2)
    val deltaY = math.pow(first.Y - second.Y, 2)
    math.sqrt(deltaX + deltaY)
  }

  def toList[T](iterator: java.util.Iterator[T]): List[T] = {
    val result = ArrayBuffer.empty[T]
    iterator.forEachRemaining(i => { result += i })
    result.toList
  }

  def getPlayersExpAndNetworth(data: Entity): Map[Int, (Int, Int)] = {
    val isRadiant = data.getDtClass.getDtName == "CDOTA_DataRadiant"

    (0 to 4).map(playerNumber => {
      val playerId = if (isRadiant) playerNumber else playerNumber + 5
      val propertyPrefix = s"m_vecDataTeam.000$playerNumber."
      val exp = data.getProperty[Int](propertyPrefix + "m_iTotalEarnedXP")
      val networth = data.getProperty[Int](propertyPrefix + "m_iNetWorth")

      playerId -> (exp, networth)
    }).toMap
  }

  def isOnCooldown(item: Entity): Boolean =
    item.getProperty[Float]("m_fCooldown") > 0.0001

  def hasEnoughMana(hero: Entity, item: Entity): Boolean =
    item.getProperty[Int]("m_iManaCost") <= hero.getProperty[Float]("m_flMana")

  def getSpawnTime(hero: Entity, time: Float): Float = {
    val respawnTime = hero.getProperty[Float]("m_flRespawnTime")
    if (respawnTime < 0) 0 else math.max(respawnTime - time, 0)
  }
}