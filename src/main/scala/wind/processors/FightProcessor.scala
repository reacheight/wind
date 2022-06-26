package wind.processors

import skadistats.clarity.model.{CombatLogEntry, Entity}
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import wind.Util
import wind.models.{Fight, GameTimeState, Location, PlayerId}
import wind.extensions._

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

class FightProcessor extends EntitiesProcessor {
  type DeathData = (GameTimeState, PlayerId, Location, Map[PlayerId, Location])
  case class DamageData(time: GameTimeState, attacker: PlayerId, target: PlayerId, damage: Int)

  def fights: Seq[Fight] = _fights

  private val _deaths: ListBuffer[DeathData] = ListBuffer.empty
  private val _damage: ListBuffer[DamageData] = ListBuffer.empty
  private var _fights: Seq[Fight] = Seq.empty

  private val TIME_DISTANCE = 20
  private val FIGHT_LOCATION_DISTANCE = 3000
  private val HERO_IN_FIGHT_DISTANCE = 1500
  private val FIGHT_START_DIFF = 8
  private val FIGHT_END_DIFF = 3
  private val DAMAGE_GROUP_THRESHOLD = 3

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_nGameState")
  def onGameEnded(gameRules: Entity, fp: FieldPath): Unit = {
    val gameState = gameRules.getPropertyForFieldPath[Int](fp)
    if (gameState != 6) return

    val splitByTime = _deaths.foldLeft(List(ArrayBuffer.empty[DeathData])) { case (fights, (deathTime, deadPlayerId, deathLocation, heroLocations)) =>
      val curFight = fights.head
      if (curFight.isEmpty || deathTime.gameTime - curFight.last._1.gameTime <= TIME_DISTANCE) {
        curFight.addOne(deathTime, deadPlayerId, deathLocation, heroLocations)
        fights
      } else
        ArrayBuffer((deathTime, deadPlayerId, deathLocation, heroLocations)) +: fights
    }

    val splitByLocation = splitByTime.flatMap(deaths => deaths.foldLeft(ListBuffer.empty[ListBuffer[DeathData]]) { case (locations, (deathTime, deadPlayerId, deathLocation, heroLocations)) =>
      locations.find(location => {
        val averageLocation = Util.getAverageLocation(location.map(_._3).toSeq)
        val distance = Util.getDistance(averageLocation, deathLocation)

        distance < FIGHT_LOCATION_DISTANCE
      }) match {
        case Some(location) => location.addOne((deathTime, deadPlayerId, deathLocation, heroLocations))
        case None => locations.addOne(ListBuffer((deathTime, deadPlayerId, deathLocation, heroLocations)))
      }

      locations
    })
      .filter(_.nonEmpty)

    _fights = splitByLocation.map(deaths => {
      val fightLocation = Util.getAverageLocation(deaths.map(_._3).toSeq)

      val heroesLocations = deaths.flatMap(_._4)
      val heroesInFight = heroesLocations
        .filter { case (_, location) => Util.getDistance(location, fightLocation) < HERO_IN_FIGHT_DISTANCE }
        .map(_._1)
        .toSet

      val firstDeathTime = deaths.head._1
      val lastDeathTime = deaths.last._1

      val damageGroupedByFightParticipants = _damage.filter(d => heroesInFight.contains(d.target))
      val damageGroupedByTime = damageGroupedByFightParticipants
        .filter(d => d.time.gameTime >= firstDeathTime.gameTime - 10 && d.time.gameTime <= lastDeathTime.gameTime + 10)
        .foldLeft(ArrayBuffer(ArrayBuffer.empty[DamageData])) { case (ranges, data) =>
          val curRange = ranges.last
          if (curRange.isEmpty || data.time.gameTime - curRange.last.time.gameTime <= DAMAGE_GROUP_THRESHOLD) {
            curRange.addOne(data)
            ranges
          } else {
            ranges.addOne(ArrayBuffer(data))
          }
        }
        .filter(range => range.last.time.gameTime - range.head.time.gameTime >= 5)

      val firstDamageRange = damageGroupedByTime.find(range => firstDeathTime.gameTime >= range.head.time.gameTime)
      val start = firstDamageRange match {
        case None => firstDeathTime.copy(gameTime = firstDeathTime.gameTime - FIGHT_START_DIFF)
        case Some(range) => range.head.time
      }

      val lastDamageRange = damageGroupedByTime.findLast(range => lastDeathTime.gameTime <= range.last.time.gameTime)
      val end = lastDamageRange match {
        case None => lastDeathTime.copy(gameTime = lastDeathTime.gameTime + FIGHT_END_DIFF)
        case Some(range) => range.last.time
      }

      val deadInFight = deaths.map(_._2).toSet

      Fight(start, end, fightLocation, heroesInFight, deadInFight)
    })
      .filter(_.radiantParticipants.nonEmpty)
      .filter(_.direParticipants.nonEmpty)
      .sortBy(_.start.gameTime)
  }

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(hero: Entity, fp: FieldPath): Unit = {
    if (!Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 1) return

    val deadPlayerId = PlayerId(hero.getProperty[Int]("m_iPlayerID"))
    val heroLocation = Util.getLocation(hero)
    val gameRules = Entities.getByDtName("CDOTAGamerulesProxy")
    val time = Util.getGameTimeState(gameRules)

    val heroes = Entities.filter(Util.isHero)
    val locations = heroes
      .filter(Util.isAlive)
      .map(hero => PlayerId(hero.getProperty[Int]("m_iPlayerID")) -> Util.getLocation(hero))
      .toMap
      .updated(deadPlayerId, heroLocation)

    _deaths.addOne((time, deadPlayerId, Util.getLocation(hero), locations))
  }

  @OnCombatLogEntry
  def onHeroDamage(ctx: Context, cle: CombatLogEntry): Unit = {
    if (isHeroDamagedAnotherHeroEvent(cle)) {
      val heroesProcessor = ctx.getProcessor(classOf[HeroProcessor])
      val time = Util.getGameTimeState(Entities.getByDtName("CDOTAGamerulesProxy"))
      val attacker = PlayerId(heroesProcessor.combatLogNameToPlayerId(cle.getDamageSourceName))
      val target = PlayerId(heroesProcessor.combatLogNameToPlayerId(cle.getTargetName))
      val damage = cle.getValue

      _damage.addOne(DamageData(time, attacker, target, damage))
    }
  }

  private def isHeroDamagedAnotherHeroEvent(cle: CombatLogEntry): Boolean =
    cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_DAMAGE &&
      cle.getDamageSourceName.startsWith("npc_dota_hero") && cle.getTargetName.startsWith("npc_dota_hero") &&
      cle.getDamageSourceName != cle.getTargetName
}
