package windota.processors

import skadistats.clarity.model.{Entity, StringTable}
import skadistats.clarity.processor.entities.OnEntityCreated
import skadistats.clarity.processor.stringtables.OnStringTableCreated
import windota.Util

class CurrentMapProcessor {
  @OnStringTableCreated
  def onStringTableCreated(ind: Int, table: StringTable) = {
    println(table.getName)
  }

  @OnEntityCreated(classPattern = "CDOTA_Unit_Fountain")
  def onFountainCreated(fountain: Entity) =
    println(s"${Util.getTeam(fountain)} fountain: ${Util.getLocation(fountain)}")

  @OnEntityCreated(classPattern = "CDOTA_BaseNPC_Fort")
  def onAncientCreated(ancient: Entity) =
    println(s"${Util.getTeam(ancient)} ancient: ${Util.getLocation(ancient)}")

  @OnEntityCreated(classPattern = "CDOTA_BaseNPC_Tower")
  def onTowerCreated(tower: Entity) =
    println(s"${Util.getTeam(tower)} tower: ${Util.getLocation(tower)}")

  @OnEntityCreated(classPattern = "CDOTA_BaseNPC_Barracks")
  def onBarracksCreated(barracks: Entity) =
    println(s"${Util.getTeam(barracks)} barracks: ${Util.getLocation(barracks)}")

  @OnEntityCreated(classPattern = "CDOTA_BaseNPC_Watch_Tower")
  def onOutpostCreated(outpost: Entity) =
    println(s"${Util.getTeam(outpost)} outpost: ${Util.getLocation(outpost)}")

  @OnEntityCreated(classPattern = "CDOTA_BaseNPC_Shop")
  def onShopCreated(shop: Entity) =
    println(s"Shop: ${Util.getLocation(shop)}")

  @OnEntityCreated(classPattern = "CDOTA_RoshanSpawner")
  def onRoshanSpawnerCreated(roshanSpawner: Entity) =
    println(s"Roshan spawner: ${Util.getLocation(roshanSpawner)}")

  @OnEntityCreated(classPattern = "CDOTA_NeutralSpawner")
  def onNeutralSpawnerCreated(neutralSpawner: Entity) =
    println(s"Type ${neutralSpawner.getProperty[Int]("m_Type")} Neutral spawner: ${Util.getLocation(neutralSpawner)}")

//  @OnEntityCreated(classPattern = "CDOTA_NPC_Observer_Ward_TrueSight")
//  def onSentryPlaced(sentry: Entity) =
//    println(s"Sentry: ${Util.getLocation(sentry)}")
}
