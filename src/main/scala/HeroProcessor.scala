package wind

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityCreated
import skadistats.clarity.processor.runner.Context

class HeroProcessor {
  private val nullValue = 16777215
  private val replicatingPropertyName = "m_hReplicatingOtherHeroModel"

  var heroNameMap: Map[Int, String] = Map()
  var heroHandleMap: Map[Int, Int] = Map()

  @OnEntityCreated(classPattern = "CDOTA_Unit_Hero_.*")
  def onHeroCreated(ctx: Context, e: Entity): Unit = {
    val isHero = e.hasProperty(replicatingPropertyName) && e.getProperty[Int](replicatingPropertyName) == nullValue
    if (!isHero) return

    val playerId = e.getProperty[Int]("m_iPlayerID")
    val heroName = e.getDtClass.getDtName.replace("CDOTA_Unit_Hero_", "")
    val heroHandle = e.getHandle

    heroNameMap += (playerId -> heroName)
    heroHandleMap += (playerId -> heroHandle)
  }
}
