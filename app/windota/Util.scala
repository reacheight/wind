package windota

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.Entities
import windota.extensions.EntitiesExtension
import windota.external.stratz.models.Position._
import windota.models.Attribute.{Agility, Attribute, Intelligence, Strength}
import windota.models.Lane.Lane
import windota.models.Role.{MidLane, OffLane, Role, SafeLane}
import windota.models.{GameTimeState, Lane, Location, PlayerId, Team}
import windota.models.Team._

object Util {
  val NullValue = 16777215
  val PlayerIds = 0 to 18 by 2
  val RadiantPlayerIds = PlayerIds.take(5)
  val DirePlayerIds = PlayerIds.takeRight(5)
  val GlyphCooldown = 300

  private val TIME_EPS: Float = 0.001f
  private val replicatingPropertyName = "m_hReplicatingOtherHeroModel"
  private val glyphCooldownPropertyName: Map[Team, String] =
    Map(Radiant -> "m_pGameRules.m_fGoodGlyphCooldown", Dire -> "m_pGameRules.m_fBadGlyphCooldown")

  def isHero(entity: Entity): Boolean =
    entity.getDtClass.getDtName.startsWith("CDOTA_Unit_Hero") &&
      entity.hasProperty(replicatingPropertyName) &&
      (entity.getProperty[Int](replicatingPropertyName) == NullValue || entity.getDtClass.getDtName.contains("Morphling")) // looks like for morph illusions returns true

  def isTower(entity: Entity): Boolean =
    entity.getDtClass.getDtName == "CDOTA_BaseNPC_Tower"

  def getTeam(entity: Entity): Team = {
    val teamNum = entity.getProperty[Int]("m_iTeamNum")
    Team(teamNum - 2)
  }

  def getPlayerId(entity: Entity): PlayerId = PlayerId(entity.getProperty[Int]("m_iPlayerID"))

  def isAlive(entity: Entity): Boolean = entity.getProperty[Int]("m_lifeState") == 0

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
    case Location(x, y) if y > 10000 && x < 6000 => Lane.Top
    case Location(x, y) if y > 10000 && y < 14000 && x > 6000 && x < 12000 => Lane.DireJungle
    case Location(x, y) if y > 6000 && y < 10000 && x > 6000 && x < 10000 => Lane.Middle
    case Location(x, y) if y > 2000 && y < 6000 && x > 4000 && x < 10000 => Lane.RadiantJungle
    case Location(x, y) if y < 6000 && x > 10000 => Lane.Bot
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

  def getPlayersExpAndNetworth(data: Entity): Map[Int, (Int, Int)] = {
    val isRadiant = data.getDtClass.getDtName == "CDOTA_DataRadiant"

    Util.RadiantPlayerIds.map(playerNumber => {
      val playerId = if (isRadiant) playerNumber else playerNumber + 10
      // in 7.31 playerId was multiplied by 2, but ids in Data-entities wasn't changed
      val propertyPrefix = s"m_vecDataTeam.000${playerNumber / 2}."
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
    if (Util.isAlive(hero))
      0
    else {
      val respawnTime = hero.getProperty[Float]("m_flRespawnTime")
      if (respawnTime < 0) 0 else math.max(respawnTime - time, 0)
    }
  }

  def getOppositeTeam(team: Team): Team = team match {
    case Radiant => Dire
    case Dire => Radiant
  }

  def isCoreRole(role: Role): Boolean = role == SafeLane || role == MidLane || role == OffLane
  def isCorePosition(position: Position) = position == Pos1 || position == Pos2 || position == Pos3

  def getOppositeCoreRole(role: Role): Role = role match {
    case MidLane => MidLane
    case SafeLane => OffLane
    case OffLane => SafeLane
  }

  def getOppositeCorePosition(position: Position) = position match {
    case Pos2 => Pos2
    case Pos1 => Pos3
    case Pos3 => Pos1
  }

  implicit class EntityExtension2(val entity: Entity) extends AnyVal {
    def primaryAttribute: Attribute = {
      val attribute = entity.getProperty[Int]("m_iPrimaryAttribute")
      attribute match {
        case 0 => Strength
        case 1 => Agility
        case 2 => Intelligence
      }
    }

    def team: Team = Util.getTeam(entity)
    def location: Location = Util.getLocation(entity)
    def isTower: Boolean = Util.isTower(entity)
    def isHero: Boolean = Util.isHero(entity)
    def isAlive: Boolean = Util.isAlive(entity)
    def playerId: PlayerId = Util.getPlayerId(entity)
    def maxMana: Float = entity.getProperty[Float]("m_flMaxMana")
    def currentMana: Float = entity.getProperty[Float]("m_flMana")
    def manaCost: Float = entity.getProperty[Int]("m_iManaCost")
    def getSpawnTime(now: GameTimeState): Int = Util.getSpawnTime(entity, now.gameTime).toInt
  }
}