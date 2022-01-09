package wind.processors

import skadistats.clarity.event.Insert
import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged, UsesEntities}
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.processor.stringtables.{StringTables, UsesStringTable}
import wind.models.PlayerId
import wind.{GameTimeState, Util}

import scala.collection.mutable.ListBuffer

@UsesStringTable("EntityNames")
@UsesEntities
class ItemUsageProcessor {
  def deathsWithBKB: Seq[(GameTimeState, PlayerId)] = _deathsWithBKB.toSeq
  def deathsWithEssenceRing: Seq[(GameTimeState, PlayerId)] = _deathsWithEssenceRing.toSeq

  @Insert
  private val entities: Entities = null

  private val _deathsWithBKB: ListBuffer[(GameTimeState, PlayerId)] = ListBuffer.empty
  private val _deathsWithEssenceRing: ListBuffer[(GameTimeState, PlayerId)] = ListBuffer.empty

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(ctx: Context, hero: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    if (!Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 2) return

    val time = Util.getGameTimeState(entities.getByDtName("CDOTAGamerulesProxy"))
    val playerId = PlayerId(hero.getProperty[Int]("m_iPlayerID"))

    findBKB(ctx, hero)
      .filterNot(isOnCooldown)
      .foreach(_ => _deathsWithBKB.addOne(time, playerId))

    findEssenceRing(hero)
      .filterNot(isOnCooldown)
      .foreach(_ => _deathsWithEssenceRing.addOne(time, playerId))
  }

  private def isOnCooldown(item: Entity): Boolean = item.getProperty[Float]("m_fCooldown") > 0.0001

  private def findBKB(ctx: Context, hero: Entity): Option[Entity] = {
    val stringTable = ctx.getProcessor(classOf[StringTables]).forName("EntityNames")
    (0 to 5)
      .map(i => hero.getProperty[Int](s"m_hItems.000$i"))
      .filter(_ != Util.NullValue)
      .map(entities.getByHandle)
      .find(item => stringTable.getNameByIndex(item.getProperty[Int]("m_pEntity.m_nameStringableIndex")) == "item_black_king_bar")
  }

  private def findEssenceRing(hero: Entity): Option[Entity] = {
    Some(hero.getProperty[Int](s"m_hItems.00016"))
      .filter(_ != Util.NullValue)
      .map(entities.getByHandle)
      .find(item => item.getDtClass.getDtName == "CDOTA_Item_Essence_Ring")
      .filter(item => item.getProperty[Int]("m_iManaCost") <= hero.getProperty[Float]("m_flMana"))
  }
}
