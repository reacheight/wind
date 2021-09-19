package wind

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityCreated
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.Demo.CDemoFileInfo

class HeroProcessor(replayInfo: CDemoFileInfo) {
  private val dota = replayInfo.getGameInfo.getDota

  var heroNameMap: Map[Int, String] = Map()
  var heroHandleMap: Map[Int, Int] = Map()
  var combatLogHeroNameToPlayerId: Map[String, Int] = Map()

  for (idx <- 0 to 9) {
    val playerInfo = dota.getPlayerInfo(idx)
    combatLogHeroNameToPlayerId += (playerInfo.getHeroName -> idx)
  }

  @OnEntityCreated(classPattern = "CDOTA_Unit_Hero_.*")
  def onHeroCreated(ctx: Context, e: Entity): Unit = {
    if (!Util.isHero(e)) return

    val playerId = e.getProperty[Int]("m_iPlayerID")
    val heroName = e.getDtClass.getDtName.replace("CDOTA_Unit_Hero_", "")
    val heroHandle = e.getHandle

    heroNameMap += (playerId -> heroName)
    heroHandleMap += (playerId -> heroHandle)
  }
}
