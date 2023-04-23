package windota.processors

import skadistats.clarity.event.Insert
import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.processor.stringtables.{StringTables, UsesStringTable}
import windota.Util
import windota.extensions.{EntitiesExtension, FieldPath}
import windota.models._

import scala.collection.mutable.ListBuffer

@UsesStringTable("EntityNames")
class ItemUsageProcessor(checkUsage: Boolean) extends EntitiesProcessor {
  def unusedItems: Seq[(GameTimeState, PlayerId, Int)] = _unusedItems.toSeq
  def unusedOnAllyItems: Seq[(GameTimeState, PlayerId, PlayerId, Int)] = _unusedOnAllyItems.toSeq

  @Insert
  private val ctx: Context = null

  private val GeneralItemName = "CDOTA_Item"

  private val _unusedItems: ListBuffer[(GameTimeState, PlayerId, Int)] = ListBuffer.empty
  private val _unusedOnAllyItems: ListBuffer[(GameTimeState, PlayerId, PlayerId, Int)] = ListBuffer.empty

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(hero: Entity, fp: FieldPath): Unit = {
    if (!checkUsage || !Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 1) return

    val gameRules = Entities.getByDtName("CDOTAGamerulesProxy")
    if (Util.getSpawnTime(hero, Time) < 10) return

    val time = TimeState
    val playerId = PlayerId(hero.getProperty[Int]("m_iPlayerID"))
    val items = getItems(hero)

    addUnusedItem("item_black_king_bar", 116)
    addUnusedItem("CDOTA_Item_Essence_Ring", 359)
    addUnusedItem("CDOTA_Item_Mekansm", 79)
    addUnusedItem("CDOTA_Item_Guardian_Greaves", 231)
    addUnusedItem("CDOTA_Item_GlimmerCape", 254)
    addUnusedItem("item_lotus_orb", 226)
    addUnusedItem("CDOTA_Item_Cyclone", 100)
    addUnusedItem("CDOTA_Item_Wind_Waker", 610)
    addUnusedItem("CDOTA_Item_ForceStaff", 102)
    addUnusedItem("CDOTA_Item_Hurricane_Pike", 263)
    addUnusedItem("CDOTA_Item_MantaStyle", 147)
    addUnusedItem("item_satanic", 156)
    addUnusedItem("CDOTA_Item_Bloodstone", 121)
    addUnusedItem("item_eternal_shroud", 692)
    addUnusedItem("item_blade_mail", 127)
    addUnusedItem("CDOTA_Item_Crimson_Guard", 242)
    addUnusedItem("item_hood_of_defiance", 131)
    addUnusedItem("CDOTA_Item_Trickster_Cloak", 571)
    addUnusedItem("CDOTA_Item_Pipe", 90)

    def addUnusedItem(entityName: String, itemId: Int): Unit =
      findUnusedItem(hero, items, entityName)
        .foreach(_ => _unusedItems.addOne((time, playerId, itemId)))


    val allies = Entities.filter(Util.isHero)
      .filter(h => h.getProperty[Int]("m_iTeamNum") == hero.getProperty[Int]("m_iTeamNum"))
      .filter(Util.isAlive)
      .filter(h => h.getHandle != hero.getHandle)

    addUnusedOnAllyItem("CDOTA_Item_Mekansm", 79, 1200)
    addUnusedOnAllyItem("CDOTA_Item_Guardian_Greaves", 231, 1200)
    addUnusedOnAllyItem("CDOTA_Item_GlimmerCape", 254, 600)
    addUnusedOnAllyItem("CDOTA_Item_Holy_Locket", 269, 500)
    addUnusedOnAllyItem("item_lotus_orb", 226, 900)
    addUnusedOnAllyItem("CDOTA_Item_Wind_Waker", 610, 1100)
    addUnusedOnAllyItem("CDOTA_Item_ForceStaff", 102, 550)
    addUnusedOnAllyItem("CDOTA_Item_Hurricane_Pike", 263, 650)
    addUnusedOnAllyItem("CDOTA_Item_Crimson_Guard", 242, 1200)
    addUnusedOnAllyItem("CDOTA_Item_Pipe", 90, 1200)

    def addUnusedOnAllyItem(entityName: String, itemId: Int, castRange: Int): Unit = {
      allies.foreach(ally => {
        val allyPlayerId = PlayerId(ally.getProperty[Int]("m_iPlayerID"))
        findUnusedItem(ally, getItems(ally), entityName)
          .filter(_ => {
            val distance = Util.getDistance(hero, ally)
            val itemCastRange = castRange + getAdditionalCastRange(ally)
            itemCastRange >= distance
          })
          .foreach(_ => _unusedOnAllyItems.addOne(time, playerId, allyPlayerId, itemId))
      })
    }
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
      .flatMap(Entities.get)
  }

  def findItem(items: Seq[Entity], name: String): Option[Entity] = {
    val stringTable = ctx.getProcessor(classOf[StringTables]).forName("EntityNames")
    items.find(item => item.getDtClass.getDtName == name ||
      item.getDtClass.getDtName == GeneralItemName &&
        stringTable.getNameByIndex(item.getProperty[Int]("m_pEntity.m_nameStringableIndex")) == name)
  }

  def getAdditionalCastRange(hero: Entity): Int = {
    val castRangeItems = Seq(
      ("CDOTA_Item_Aether_Lens", 225),
      ("item_octarine_core", 225),
      ("CDOTA_Item_Keen_Optic", 75),
      ("CDOTA_Item_Psychic_Headband", 100),
      ("CDOTA_Item_Seer_Stone", 350),
      ("CDOTA_Item_Telescope", 110),
    )

    val items = getItems(hero)
    castRangeItems.filter(item => findItem(items, item._1).nonEmpty).map(_._2).sum
  }

  def getCastRangeIfHasBlink(hero: Entity): Int = {
    val blinkItemName = "CDOTA_Item_BlinkDagger"
    val items = getItems(hero)
    findItem(items, blinkItemName)
      .filterNot(Util.isOnCooldown)
      .map(_ => 1200).getOrElse(0)
  }
}
