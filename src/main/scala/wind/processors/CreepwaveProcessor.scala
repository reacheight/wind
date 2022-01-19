package wind.processors

import skadistats.clarity.model.{CombatLogEntry, Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import wind.{GameTimeState, Util}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class CreepwaveProcessor {
  def wastedCreepwaves: Seq[(GameTimeState, String)] = _wastedCreepwaves

  private var _wastedCreepwaves: Seq[(GameTimeState, String)] = Seq.empty
  private val creepDeaths: mutable.Map[String, ListBuffer[GameTimeState]] = mutable.Map.empty

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_nGameState")
  def onGameEnded(gameRules: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameState = gameRules.getPropertyForFieldPath[Int](fp)
    if (gameState != 6) return

    _wastedCreepwaves = creepDeaths.map { case (tower, deaths) =>
      val formattedTower = formatTowerName(tower)
      val (ranges, last) = deaths.foldLeft(Seq.empty[Seq[GameTimeState]], Seq.empty[GameTimeState]) { case ((all, cur), death) =>
        if (cur.isEmpty || death.gameTime - cur.last.gameTime <= 10)
          (all, cur :+ death)
        else
          (all :+ cur, Seq(death))
      }

      (ranges :+ last)
        .filter(range => range.length >= 4)
        .map(range => (range.head, formattedTower))
    }.flatten.toSeq.sortBy(_._1.gameTime)
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

  def formatTowerName(towerName: String): String = {
    val teamPrefix = if (towerName.contains("goodguys")) "Radiant" else "Dire"
    val newName = towerName
      .replace("npc_dota_badguys_tower", "")
      .replace("npc_dota_goodguys_tower", "")

    if (newName == "4")
      s"${teamPrefix} T4"
    else {
      val tokens = newName.split("_")
      val tier = tokens(0)
      val lane = tokens(1)
      val laneCapitalized = lane(0).toUpper + lane.substring(1)

      s"${teamPrefix} $laneCapitalized T$tier"
    }
  }
}
