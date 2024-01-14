package windota.processors

import skadistats.clarity.model.{Entity, StringTable}
import skadistats.clarity.processor.entities.OnEntityCreated
import skadistats.clarity.processor.stringtables.OnStringTableCreated
import windota.Util

class CurrentMapProcessor {
//  @OnStringTableCreated
//  def onStringTableCreated(ind: Int, table: StringTable) = {
//    println(table.getName)
//  }

  @OnEntityCreated(classPattern = "CDOTA_Unit_Fountain")
  def onFountainCreated(fountain: Entity) =
    println(s"${Util.getTeam(fountain)} fountain: ${Util.getLocationNew(fountain)}")

  @OnEntityCreated(classPattern = "CDOTA_BaseNPC_Fort")
  def onAncientCreated(ancient: Entity) =
    println(s"${Util.getTeam(ancient)} ancient: ${Util.getLocationNew(ancient)}")

  @OnEntityCreated(classPattern = "CDOTA_BaseNPC_Tower")
  def onTowerCreated(tower: Entity) =
    println(s"${Util.getTeam(tower)} tower: ${Util.getLocationNew(tower)}")

  @OnEntityCreated(classPattern = "CDOTA_BaseNPC_Barracks")
  def onBarracksCreated(barracks: Entity) =
    println(s"${Util.getTeam(barracks)} barracks: ${Util.getLocationNew(barracks)}")

  @OnEntityCreated(classPattern = "CDOTA_BaseNPC_Watch_Tower")
  def onOutpostCreated(outpost: Entity) =
    println(s"${Util.getTeam(outpost)} outpost: ${Util.getLocationNew(outpost)}")

  @OnEntityCreated(classPattern = "CDOTA_BaseNPC_Shop")
  def onShopCreated(shop: Entity) =
    println(s"Shop: ${Util.getLocationNew(shop)}")

  @OnEntityCreated(classPattern = "CDOTA_RoshanSpawner")
  def onRoshanSpawnerCreated(roshanSpawner: Entity) =
    println(s"Roshan spawner: ${Util.getLocationNew(roshanSpawner)}")

//  @OnEntityCreated(classPattern = "CDOTA_NeutralSpawner")
//  def onNeutralSpawnerCreated(neutralSpawner: Entity) =
//    println(s"Type ${neutralSpawner.getProperty[Int]("m_Type")} Neutral spawner: ${Util.getLocationNew(neutralSpawner)}")

//  @OnEntityCreated(classPattern = "CDOTA_NPC_Observer_Ward_TrueSight")
//  def onSentryPlaced(sentry: Entity) =
//    println(s"Sentry: ${Util.getLocationNew(sentry)}")
}
