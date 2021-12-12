package wind.processors

import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}
import skadistats.clarity.processor.runner.Context
import wind.Team._
import wind.Util

import scala.collection.{immutable, mutable}

class GlyphProcessor {
  def glyphNotUsedOnT1: immutable.Map[Team, Int] = _glyphNotUsedOnT1.toMap

  private val _glyphNotUsedOnT1 = mutable.Map[Team, Int]()

  @OnEntityPropertyChanged(classPattern = "CDOTA_BaseNPC_Tower", propertyPattern = "m_lifeState")
  def onTowerLifeStateChanged(ctx: Context, tower: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    if (tower.getProperty[Int]("m_iCurrentLevel") == 1 && tower.getPropertyForFieldPath[Int](fp) == 1) {
      val towerTeam = if (tower.getProperty[Int]("m_iTeamNum") == 2) Radiant else Dire
      val gameRules = ctx.getProcessor(classOf[Entities]).getByDtName("CDOTAGamerulesProxy")
      if (!Util.isGlyphOnCooldown(gameRules, towerTeam)) {
        val current = _glyphNotUsedOnT1.getOrElse(towerTeam, 0)
        _glyphNotUsedOnT1 += towerTeam -> (current + 1)
      }
    }
  }
}
