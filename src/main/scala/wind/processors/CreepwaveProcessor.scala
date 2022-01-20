package wind.processors

import skadistats.clarity.model.{CombatLogEntry, Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import wind.models.Lane.{Bot, Lane, Middle, Top}
import wind.models.Team.{Dire, Radiant, Team}
import wind.Util
import wind.models.GameTimeState

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class CreepwaveProcessor {
  def wastedCreepwaves: Seq[(GameTimeState, Team, Lane, Int)] = _wastedCreepwaves

  private var _wastedCreepwaves: Seq[(GameTimeState, Team, Lane, Int)] = Seq.empty
  private val creepDeaths: mutable.Map[String, ListBuffer[GameTimeState]] = mutable.Map.empty

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_nGameState")
  def onGameEnded(gameRules: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameState = gameRules.getPropertyForFieldPath[Int](fp)
    if (gameState != 6) return

    _wastedCreepwaves = creepDeaths.flatMap { case (tower, deaths) =>
      val (team, lane, tier) = parseTowerName(tower)
      val (ranges, last) = deaths.foldLeft(Seq.empty[Seq[GameTimeState]], Seq.empty[GameTimeState]) { case ((all, cur), death) =>
        if (cur.isEmpty || death.gameTime - cur.last.gameTime <= 10)
          (all, cur :+ death)
        else
          (all :+ cur, Seq(death))
      }

      (ranges :+ last)
        .filter(range => range.length >= 4)
        .map(range => (range.head, team, lane, tier))
    }.toSeq.sortBy(_._1.gameTime)
  }

  @OnCombatLogEntry
  def onCreepDied(ctx: Context, cle: CombatLogEntry): Unit = {
    if (towerKilledCreep(cle)) {
      val gameRules = ctx.getProcessor(classOf[Entities]).getByDtName("CDOTAGamerulesProxy")
      val time = Util.getGameTimeState(gameRules)
      addCreepDeath(cle.getAttackerName, time)
    }
  }

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
