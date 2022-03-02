package wind
package processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityCreated
import skadistats.clarity.wire.common.proto.Demo.CDemoFileInfo

class HeroProcessor(replayInfo: CDemoFileInfo) {
  private val game = replayInfo.getGameInfo.getDota

  def heroName: Map[Int, String] = heroNameBuilder.result
  def heroHandle: Map[Int, Int] = heroHandleBuilder.result
  val combatLogNameToPlayerId: Map[String, Int] = (0 to 9).map(id => game.getPlayerInfo(id).getHeroName -> id * 2).toMap

  private val heroNameBuilder = Map.newBuilder[Int, String]
  private val heroHandleBuilder = Map.newBuilder[Int, Int]

  @OnEntityCreated(classPattern = "CDOTA_Unit_Hero_.*")
  def onHeroCreated(hero: Entity): Unit = {
    if (!Util.isHero(hero)) return

    val playerId = hero.getProperty[Int]("m_iPlayerID")
    val name = hero.getDtClass.getDtName.replace("CDOTA_Unit_Hero_", "")
    val handle = hero.getHandle

    heroNameBuilder += (playerId -> name)
    heroHandleBuilder += (playerId -> handle)
  }
}
