package windota.processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.stringtables.UsesStringTable
import windota.Util
import windota.extensions.{EntitiesExtension, FieldPath}
import windota.models.{ItemId, PlayerId}
import windota.models.internal.UnusedItem

import scala.collection.mutable.ListBuffer

@UsesStringTable("EntityNames")
class ItemUsageProcessor extends ProcessorBase {
  def unusedItems: Seq[UnusedItem] = _unusedItems.toSeq
  private val _unusedItems: ListBuffer[UnusedItem] = ListBuffer.empty

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(hero: Entity, fp: FieldPath): Unit = {
    if (!Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 1) return

    val gameTimeState = GameTimeHelper.State
    if (Util.getSpawnTime(hero, gameTimeState.gameTime) < 10) return

    val playerId = PlayerId(hero.getProperty[Int]("m_iPlayerID"))
    val items = ItemsHelper.getItems(hero)

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
    addUnusedItem("item_blade_mail", 127)
    addUnusedItem("CDOTA_Item_Crimson_Guard", 242)
    addUnusedItem("item_hood_of_defiance", 131)
    addUnusedItem("CDOTA_Item_Trickster_Cloak", 571)
    addUnusedItem("CDOTA_Item_Pipe", 90)
    addUnusedItem("CDOTA_Item_Pavise", 90)

    def addUnusedItem(entityName: String, itemId: Int): Unit =
      ItemsHelper.findUnusedItem(hero, items, entityName)
        .foreach(_ => _unusedItems.addOne(UnusedItem(playerId, playerId, ItemId(itemId), gameTimeState, withBlink = false)))


    val allies = Entities.filter(Util.isHero)
      .filter(h => h.getProperty[Int]("m_iTeamNum") == hero.getProperty[Int]("m_iTeamNum"))
      .filter(Util.isAlive)
      .filter(h => h.getHandle != hero.getHandle)

    addUnusedOnAllyItem("CDOTA_Item_Mekansm", 79, 1200, true)
    addUnusedOnAllyItem("CDOTA_Item_Guardian_Greaves", 231, 1200, true)
    addUnusedOnAllyItem("CDOTA_Item_GlimmerCape", 254, 600)
    addUnusedOnAllyItem("CDOTA_Item_Holy_Locket", 269, 500)
    addUnusedOnAllyItem("item_lotus_orb", 226, 900)
    addUnusedOnAllyItem("CDOTA_Item_Wind_Waker", 610, 550)
    addUnusedOnAllyItem("CDOTA_Item_ForceStaff", 102, 550)
    addUnusedOnAllyItem("CDOTA_Item_Hurricane_Pike", 263, 650)
    addUnusedOnAllyItem("CDOTA_Item_Crimson_Guard", 242, 1200, true)
    addUnusedOnAllyItem("CDOTA_Item_Pipe", 90, 1200, true)

    def addUnusedOnAllyItem(entityName: String, itemId: Int, castRange: Int, isAura: Boolean = false): Unit = {
      allies.foreach(ally => {
        val allyPlayerId = PlayerId(ally.getProperty[Int]("m_iPlayerID"))
        ItemsHelper.findUnusedItem(ally, ItemsHelper.getItems(ally), entityName)
          .filter(_ => {
            val distance = Util.getDistance(hero, ally)
            val itemCastRange = castRange + (if (isAura) 0 else ItemsHelper.getAdditionalCastRange(ally))
            itemCastRange >= distance
          })
          .foreach(_ => _unusedItems.addOne(UnusedItem(allyPlayerId, playerId, ItemId(itemId), gameTimeState, withBlink = false)))
      })
    }
  }
}
