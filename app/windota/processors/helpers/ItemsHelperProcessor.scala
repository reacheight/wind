package windota.processors.helpers

import skadistats.clarity.event.Insert
import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.Entities
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.processor.stringtables.{StringTables, UsesStringTable}
import windota.Util
import windota.extensions._

@UsesStringTable("EntityNames")
class ItemsHelperProcessor {
  private val GeneralItemName = "CDOTA_Item"

  private val CastRangeItems = Seq(
    ("CDOTA_Item_Aether_Lens", 225),
    ("item_octarine_core", 225),
    ("CDOTA_Item_Keen_Optic", 75),
    ("CDOTA_Item_Psychic_Headband", 100),
    ("CDOTA_Item_Seer_Stone", 350),
    ("CDOTA_Item_Telescope", 110),
  )

  @Insert
  private val ctx: Context = null

  @Insert
  protected val Entities: Entities = null

  def getItems(hero: Entity): Seq[Entity] = {
    ((0 to 5) ++ Seq(16))
      .map(i => hero.getProperty[Int](s"m_hItems.000$i"))
      .filter(_ != Util.NullValue)
      .flatMap(Entities.get)
  }

  def findItem(items: Seq[Entity], name: String): Option[Entity] = {
    val stringTable = ctx.getProcessor(classOf[StringTables]).forName("EntityNames")
    items.find(item => item.getDtClass.getDtName == name ||
      item.getDtClass.getDtName == GeneralItemName &&
        stringTable.getNameByIndex(item.getProperty[Int]("m_pEntity.m_nameStringableIndex")) == name)
  }

  def findItem(hero: Entity, name: String): Option[Entity] = findItem(getItems(hero), name)

  def findUnusedItem(hero: Entity, items: Seq[Entity], name: String): Option[Entity] = {
    findItem(items, name)
      .filter(item => Util.hasEnoughMana(hero, item))
      .filterNot(Util.isOnCooldown)
  }

  def getAdditionalCastRange(hero: Entity): Int = {
    val items = getItems(hero)
    CastRangeItems.filter(item => findItem(items, item._1).nonEmpty).map(_._2).sum
  }
}
