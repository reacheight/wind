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
  def deathsWithMekansm: Seq[(GameTimeState, PlayerId)] = _deathsWithMekansm.toSeq
  def deathsWithGreaves: Seq[(GameTimeState, PlayerId)] = _deathsWithGreaves.toSeq

  @Insert
  private val entities: Entities = null
  @Insert
  private val ctx: Context = null

  private val GeneralItemName = "CDOTA_Item"

  private val _deathsWithBKB: ListBuffer[(GameTimeState, PlayerId)] = ListBuffer.empty
  private val _deathsWithEssenceRing: ListBuffer[(GameTimeState, PlayerId)] = ListBuffer.empty
  private val _deathsWithMekansm: ListBuffer[(GameTimeState, PlayerId)] = ListBuffer.empty
  private val _deathsWithGreaves: ListBuffer[(GameTimeState, PlayerId)] = ListBuffer.empty

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(hero: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    if (!Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 2) return

    val time = Util.getGameTimeState(entities.getByDtName("CDOTAGamerulesProxy"))
    val playerId = PlayerId(hero.getProperty[Int]("m_iPlayerID"))
    val items = getItems(hero)

    findUnusedItem(hero, items, "item_black_king_bar").foreach(_ => _deathsWithBKB.addOne(time, playerId))
    findUnusedItem(hero, items, "CDOTA_Item_Essence_Ring").foreach(_ => _deathsWithEssenceRing.addOne(time, playerId))
    findUnusedItem(hero, items, "CDOTA_Item_Mekansm").foreach(_ => _deathsWithMekansm.addOne(time, playerId))
    findUnusedItem(hero, items, "CDOTA_Item_Guardian_Greaves").foreach(_ => _deathsWithGreaves.addOne(time, playerId))
  }

  private def findUnusedItem(hero: Entity, items: Seq[Entity], name: String): Option[Entity] = {
    findItem(items, name)
      .filter(item => hasEnoughMana(hero, item))
      .filterNot(isOnCooldown)
  }

  private def isOnCooldown(item: Entity): Boolean =
    item.getProperty[Float]("m_fCooldown") > 0.0001

  private def hasEnoughMana(hero: Entity, item: Entity): Boolean =
    item.getProperty[Int]("m_iManaCost") <= hero.getProperty[Float]("m_flMana")

  private def getItems(hero: Entity): Seq[Entity] = {
    ((0 to 5) ++ Seq(16))
      .map(i => hero.getProperty[Int](s"m_hItems.000$i"))
      .filter(_ != Util.NullValue)
      .map(entities.getByHandle)
  }

  private def findItem(items: Seq[Entity], name: String): Option[Entity] = {
    val stringTable = ctx.getProcessor(classOf[StringTables]).forName("EntityNames")
    items.find(item => item.getDtClass.getDtName == name ||
      item.getDtClass.getDtName == GeneralItemName &&
        stringTable.getNameByIndex(item.getProperty[Int]("m_pEntity.m_nameStringableIndex")) == name)
  }
}
