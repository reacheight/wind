package wind.processors

import skadistats.clarity.model.{CombatLogEntry, Entity}
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import wind.Util
import wind.models.Lane.{Bot, Lane, Middle, Top}
import wind.models.Team.{Dire, Radiant, Team}
import wind.models.{GameTimeState, PlayerId}
import wind.extensions._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class CreepwaveProcessor extends EntitiesProcessor {
  def notTankedCreepwaves: Seq[(GameTimeState, Team, Lane, Seq[PlayerId])] = _notTankedCreepwaves.toSeq
  def wastedCreepwaves: Seq[(GameTimeState, Team, Lane, Int)] = _wastedCreepwaves

  private val _notTankedCreepwaves: ListBuffer[(GameTimeState, Team, Lane, Seq[PlayerId])] = ListBuffer.empty
  private var _wastedCreepwaves: Seq[(GameTimeState, Team, Lane, Int)] = Seq.empty

  private val creepDeaths: mutable.Map[String, ListBuffer[GameTimeState]] = mutable.Map.empty
  private val lastTowerHitCreepTime: mutable.Map[(Team, Lane), GameTimeState] = mutable.Map.empty

  @OnEntityPropertyChanged(classPattern = "CDOTA_BaseNPC_Creep_Lane", propertyPattern = "m_iHealth")
  def onCreepHPChanged(creep: Entity, fp: FieldPath): Unit = {
    val time = Util.getGameTimeState(Entities.getByDtName("CDOTAGamerulesProxy"))
    if (time.gameTime > 60 * 10) return

    val towerTeam = Util.getOppositeTeam(Util.getTeam(creep))
    val tower = Entities.find(e => Util.isTower(e) && e.getProperty[Int]("m_iCurrentLevel") == 1 && Util.getTeam(e) == towerTeam && Util.getDistance(e, creep) <= 800)
    tower.foreach(t => {
      val towerLane = Util.getLane(Util.getLocation(t))
      if (towerLane == Middle) return

      if (!lastTowerHitCreepTime.contains((towerTeam, towerLane)) || time.gameTime - lastTowerHitCreepTime((towerTeam, towerLane)).gameTime > 5) {
        val nearAllyHeroes = Entities.filter(e => Util.isHero(e) && Util.getTeam(e) == towerTeam && Util.isAlive(e) && Util.getDistance(e, creep) < 500)
        val nearEnemyHeroes = Entities.filter(e => Util.isHero(e) && Util.getTeam(e) == Util.getTeam(creep) && Util.isAlive(e) && Util.getDistance(e, creep) < 1000)
        val comingAllyCreeps = Entities.filter(e => e.getDtClass.getDtName == "CDOTA_BaseNPC_Creep_Lane" && Util.getTeam(e) == towerTeam && Util.isAlive(e) && Util.getDistance(e, t) < 1000)
        val nearEnemyCreeps = Entities.filter(e => e.getDtClass.getDtName == "CDOTA_BaseNPC_Creep_Lane" && Util.getTeam(e) == Util.getTeam(creep) && Util.isAlive(e) && Util.getDistance(e, creep) < 500)

        if (comingAllyCreeps.nonEmpty && nearAllyHeroes.nonEmpty && nearEnemyHeroes.isEmpty && nearEnemyCreeps.length >= 2)
          _notTankedCreepwaves.addOne((time, towerTeam, towerLane, nearAllyHeroes.map(Util.getPlayerId)))
      }

      lastTowerHitCreepTime((towerTeam, towerLane)) = time
    })
  }

//  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_nGameState")
//  def onGameEnded(gameRules: Entity, fp: FieldPath): Unit = {
//    val gameState = gameRules.getPropertyForFieldPath[Int](fp)
//    if (gameState != 6) return
//
//    _wastedCreepwaves = creepDeaths.flatMap { case (tower, deaths) =>
//      val (team, lane, tier) = parseTowerName(tower)
//      val (ranges, last) = deaths.foldLeft(Seq.empty[Seq[GameTimeState]], Seq.empty[GameTimeState]) { case ((all, cur), death) =>
//        if (cur.isEmpty || death.gameTime - cur.last.gameTime <= 10)
//          (all, cur :+ death)
//        else
//          (all :+ cur, Seq(death))
//      }
//
//      (ranges :+ last)
//        .filter(range => range.length >= 4)
//        .map(range => (range.head, team, lane, tier))
//    }.toSeq.sortBy(_._1.gameTime)
//  }
//
//  @OnCombatLogEntry
//  def onCreepDied(ctx: Context, cle: CombatLogEntry): Unit = {
//    if (towerKilledCreep(cle)) {
//      val gameRules = Entities.getByDtName("CDOTAGamerulesProxy")
//      val time = Util.getGameTimeState(gameRules)
//      addCreepDeath(cle.getAttackerName, time)
//    }
//  }

  def addCreepDeath(tower: String, time: GameTimeState): Unit = {
    if (!creepDeaths.contains(tower))
      creepDeaths(tower) = ListBuffer.empty

    creepDeaths(tower).addOne(time)
  }

  def towerKilledCreep(cle: CombatLogEntry): Boolean =
    isCreepDied(cle) && cle.getAttackerName.contains("tower")

  def isCreepDied(cle: CombatLogEntry): Boolean =
    cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_DEATH &&
      (cle.getTargetName.contains("creep_goodguys") || cle.getTargetName.contains("creep_badguys") || cle.getTargetName.contains("siege"))

  def parseTowerName(towerName: String): (Team, Lane, Int) = {
    val team = if (towerName.contains("goodguys")) Radiant else Dire
    val tokens = towerName
      .replace("npc_dota_badguys_tower", "")
      .replace("npc_dota_goodguys_tower", "")
      .split("_")

    val tier = tokens(0).toInt
    val lane =
      if (tokens.length < 2 || tokens(1) == "mid")
        Middle
      else if (tokens(1) == "bot")
        Bot
      else
        Top

    (team, lane, tier)
  }
}
