package windota.processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityCreated
import skadistats.clarity.wire.common.proto.Demo.CDemoFileInfo
import windota.Util
import windota.constants.Heroes
import windota.models._

class HeroProcessor(replayInfo: CDemoFileInfo) {
  private val game = replayInfo.getGameInfo.getDota

  def heroName: Map[Int, String] = heroNameBuilder.result
  def heroHandle: Map[Int, Int] = heroHandleBuilder.result
  def heroId: Map[PlayerId, HeroId] = heroIdBuilder.result
  def playerId: Map[HeroId, PlayerId] = heroIdToPlayerIdBuilder.result
  val combatLogNameToPlayerId: Map[String, Int] = (0 to 9).map(id => game.getPlayerInfo(id).getHeroName -> id * 2).toMap
  val clNameToPlayerId: Map[String, PlayerId] = combatLogNameToPlayerId.map { case (clName, playerId) => clName -> PlayerId(playerId) }

  private val heroNameBuilder = Map.newBuilder[Int, String]
  private val heroHandleBuilder = Map.newBuilder[Int, Int]
  private val heroIdBuilder = Map.newBuilder[PlayerId, HeroId]
  private val heroIdToPlayerIdBuilder = Map.newBuilder[HeroId, PlayerId]

  @OnEntityCreated(classPattern = "CDOTA_Unit_Hero_.*")
  def onHeroCreated(hero: Entity): Unit = {
    if (!Util.isHero(hero)) return

    val playerId = hero.getProperty[Int]("m_iPlayerID")
    val name = hero.getDtClass.getDtName.replace("CDOTA_Unit_Hero_", "")
    val handle = hero.getHandle

    heroNameBuilder += (playerId -> name)
    heroHandleBuilder += (playerId -> handle)

    val cleName = combatLogNameToPlayerId.map(_.swap)(playerId)
    val heroId = Heroes.getId(cleName)
    heroIdBuilder += (PlayerId(playerId) -> HeroId(heroId))
    heroIdToPlayerIdBuilder += (HeroId(heroId) -> PlayerId(playerId))
  }
}
