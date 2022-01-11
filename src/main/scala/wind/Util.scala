package wind

import skadistats.clarity.model.Entity
import wind.models.Team._

import java.time.{Duration, Period}
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
          return new GameTimeState(true, true, gameTime - startTime)
        }
        else {
          val transitionTime = gameRulesEntity.getProperty[Float]("m_pGameRules.m_flStateTransitionTime")
          return new GameTimeState(true, false, gameTime - transitionTime)
        }
      }

      return new GameTimeState(false, false, Float.MinValue)
    }

    new GameTimeState(false, false, Float.MinValue)
  }

  def isGlyphOnCooldown(gameRules: Entity, team: Team): Boolean =
    gameRules.getProperty[Float]("m_pGameRules.m_fGameTime") < gameRules.getProperty[Float](glyphCooldownPropertyName(team))

  def isHero(entity: Entity): Boolean =
    entity.getDtClass.getDtName.startsWith("CDOTA_Unit_Hero") &&
      entity.hasProperty(replicatingPropertyName) &&
      entity.getProperty[Int](replicatingPropertyName) == NullValue

  def isVisibleByEnemies(entity: Entity): Boolean = entity.getProperty[Int]("m_iTaggedAsVisibleByTeam") > 10

  def getLocation(entity: Entity): (Float, Float) = {
    if (!entity.hasProperty("CBodyComponent.m_cellX") || !entity.hasProperty("CBodyComponent.m_cellY") ||
      !entity.hasProperty("CBodyComponent.m_vecX") || !entity.hasProperty("CBodyComponent.m_vecY")) {
      throw new IllegalArgumentException
    }

    val (x, y) = (entity.getProperty[Int]("CBodyComponent.m_cellX"), entity.getProperty[Int]("CBodyComponent.m_cellY"))
    val (vecX, vecY) = (entity.getProperty[Float]("CBodyComponent.m_vecX"), entity.getProperty[Float]("CBodyComponent.m_vecY"))

    (x * 128 + vecX - 8192, y * 128 + vecY - 8192)
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

class GameTimeState(val preGameStarted: Boolean, val gameStarted: Boolean, val gameTime: Float) {
  override def toString: String = {
    val minutes = gameTime.toInt.sign * gameTime.toInt.abs / 60
    val seconds = gameTime.toInt.abs % 60
    val secondsStr = if (seconds < 10) s"0$seconds" else seconds
    if (preGameStarted) s"$minutes:$secondsStr" else "not started"
  }
}
