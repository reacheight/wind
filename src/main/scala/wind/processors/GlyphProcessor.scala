package wind.processors

import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}
import skadistats.clarity.processor.runner.Context
import wind.Team._
import wind.Util

class GlyphProcessor {
  var glyphNotUsedOnT1: Map[Team, Int] = Map()

  @OnEntityPropertyChanged(classPattern = "CDOTA_BaseNPC_Tower", propertyPattern = "m_lifeState")
  def onTowerLifeStateChanged(ctx: Context, tower: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    if (tower.getProperty[Int]("m_iCurrentLevel") != 1) return
    if (tower.getPropertyForFieldPath[Int](fp) != 1) return

    val towerTeam = if (tower.getProperty[Int]("m_iTeamNum") == 2) Radiant else Dire
    val gameRules = ctx.getProcessor(classOf[Entities]).getByDtName("CDOTAGamerulesProxy")
    if (!Util.isGlyphOnCooldown(gameRules, towerTeam)) {
      val current = glyphNotUsedOnT1.getOrElse(towerTeam, 0)
      glyphNotUsedOnT1 += towerTeam -> (current + 1)
    }
  }
}
