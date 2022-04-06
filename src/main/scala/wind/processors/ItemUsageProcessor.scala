package wind.processors

import skadistats.clarity.event.Insert
import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.processor.stringtables.{StringTables, UsesStringTable}
import wind.Util
import wind.models.{GameTimeState, PlayerId}
import wind.extensions._

import scala.collection.mutable.ListBuffer

@UsesStringTable("EntityNames")
class ItemUsageProcessor extends EntitiesProcessor {
  def unusedItems: Seq[(GameTimeState, PlayerId, String)] = _unusedItems.toSeq
  def unusedOnAllyItems: Seq[(GameTimeState, PlayerId, PlayerId, String)] = _unusedOnAllyItems.toSeq

  @Insert
  private val ctx: Context = null

  private val GeneralItemName = "CDOTA_Item"

  private val _unusedItems: ListBuffer[(GameTimeState, PlayerId, String)] = ListBuffer.empty
  private val _unusedOnAllyItems: ListBuffer[(GameTimeState, PlayerId, PlayerId, String)] = ListBuffer.empty

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(hero: Entity, fp: FieldPath): Unit = {
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
    addUnusedItem("CDOTA_Item_GlimmerCape", "Glimmer Cape")

    def addUnusedItem(entityName: String, realName: String): Unit =
      findUnusedItem(hero, items, entityName)
        .foreach(_ => _unusedItems.addOne((time, playerId, realName)))


    val allies = Entities.filter(Util.isHero)
      .filter(h => h.getProperty[Int]("m_iTeamNum") == hero.getProperty[Int]("m_iTeamNum"))
      .filter(Util.isAlive)
      .filter(h => h.getHandle != hero.getHandle)

    addUnusedOnAllyItem("CDOTA_Item_Mekansm", "Mekansm", 1200)
    addUnusedOnAllyItem("CDOTA_Item_Guardian_Greaves", "Guardian Greaves", 1200)
    addUnusedOnAllyItem("CDOTA_Item_GlimmerCape", "Glimmer Cape", 550)
    addUnusedOnAllyItem("CDOTA_Item_Holy_Locket", "Holy Locket", 500)

    def addUnusedOnAllyItem(entityName: String, realName: String, castRange: Int): Unit = {
      allies.foreach(ally => {
        val allyPlayerId = PlayerId(ally.getProperty[Int]("m_iPlayerID"))
        findUnusedItem(ally, getItems(ally), entityName)
          .filter(_ => {
            val distance = Util.getDistance(hero, ally)
            val itemCastRange = castRange + getAdditionalCastRange(ally)
            itemCastRange >= distance
          })
          .foreach(_ => _unusedOnAllyItems.addOne(time, playerId, allyPlayerId, realName))
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
}
