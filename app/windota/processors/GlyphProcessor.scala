package windota.processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}
import skadistats.clarity.processor.runner.Context
import windota.Util
import windota.extensions._
import windota.models.Team._
import windota.models._

import scala.collection.mutable.ListBuffer
import scala.collection.{immutable, mutable}

class GlyphProcessor {
  def glyphNotUsedOnT1: immutable.Map[Team, Int] = _glyphNotUsedOnT1.toMap
  def glyphOnDeadT2 = _glyphOnDeadT2.map { case (team, usages) => team -> usages.toSeq }

  private val _glyphNotUsedOnT1 = mutable.Map[Team, Int]()
  private val _glyphOnDeadT2: Map[Team, ListBuffer[GameTimeState]] = Map(Radiant -> ListBuffer.empty, Dire -> ListBuffer.empty)

//  @OnEntityPropertyChanged(classPattern = "CDOTA_BaseNPC_Tower", propertyPattern = "m_lifeState")
//  def onTowerLifeStateChanged(ctx: Context, tower: Entity, fp: FieldPath): Unit = {
//    if (tower.getPropertyForFieldPath[Int](fp) != 1) return
//
//    val towerTeam = Util.getTeam(tower)
//    val gameRules = ctx.getProcessor(classOf[Entities]).getByDtName("CDOTAGamerulesProxy")
//    val tier = tower.getProperty[Int]("m_iCurrentLevel")
//
//    if (tier == 1 && !Util.isGlyphOnCooldown(gameRules, towerTeam)) {
//      val current = _glyphNotUsedOnT1.getOrElse(towerTeam, 0)
//      _glyphNotUsedOnT1 += towerTeam -> (current + 1)
//    }
//
//    if (tier == 2 && Util.getGlyphCooldown(gameRules, towerTeam) > Util.GlyphCooldown - 20)
//      _glyphOnDeadT2(towerTeam).addOne(Util.getGameTimeState(gameRules))
//  }
}
