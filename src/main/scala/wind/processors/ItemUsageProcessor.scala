package wind.processors

import skadistats.clarity.event.Insert
import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.processor.stringtables.{StringTables, UsesStringTable}
import wind.Util
import wind.models.{GameTimeState, PlayerId}

import scala.collection.mutable.ListBuffer

@UsesStringTable("EntityNames")
class ItemUsageProcessor extends EntitiesProcessor {
  def unusedItems: Seq[(GameTimeState, PlayerId, String)] = _unusedItems.toSeq

  @Insert
  private val ctx: Context = null

  private val GeneralItemName = "CDOTA_Item"

  private val _unusedItems: ListBuffer[(GameTimeState, PlayerId, String)] = ListBuffer.empty

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(hero: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    if (!Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 1) return

    val gameRules = Entities.getByDtName("CDOTAGamerulesProxy")
    if (Util.getSpawnTime(hero, gameRules.getProperty[Float]("m_pGameRules.m_fGameTime")) < 10) return

    val time = Util.getGameTimeState(gameRules)
    val playerId = PlayerId(hero.getProperty[Int]("m_iPlayerID"))
    val items = getItems(hero)

    addUnusedItem("item_black_king_bar", "BKB")
    addUnusedItem("CDOTA_Item_Essence_Ring", "Essence Ring")
    addUnusedItem("CDOTA_Item_Mekansm", "Mekansm")
    addUnusedItem("CDOTA_Item_Guardian_Greaves", "Guardian Greaves")

    def addUnusedItem(entityName: String, realName: String): Unit =
      findUnusedItem(hero, items, entityName)
        .foreach(_ => _unusedItems.addOne((time, playerId, realName)))
  }

  private def findUnusedItem(hero: Entity, items: Seq[Entity], name: String): Option[Entity] = {
    findItem(items, name)
      .filter(item => Util.hasEnoughMana(hero, item))
      .filterNot(Util.isOnCooldown)
  }

  def getItems(hero: Entity): Seq[Entity] = {
    ((0 to 5) ++ Seq(16))
      .map(i => hero.getProperty[Int](s"m_hItems.000$i"))
      .filter(_ != Util.NullValue)
      .map(Entities.getByHandle)
      .filter(_ != null)
  }

  def findItem(items: Seq[Entity], name: String): Option[Entity] = {
    val stringTable = ctx.getProcessor(classOf[StringTables]).forName("EntityNames")
    items.find(item => item.getDtClass.getDtName == name ||
      item.getDtClass.getDtName == GeneralItemName &&
        stringTable.getNameByIndex(item.getProperty[Int]("m_pEntity.m_nameStringableIndex")) == name)
  }
}
