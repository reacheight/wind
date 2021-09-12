package wind

import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{OnEntityCreated, OnEntityPropertyChanged}
import skadistats.clarity.processor.runner.Context

class CourierProcessor {
  private val nullValue = 16777215
  private var gameStarted = false

  var heroMap : Map[Int, String] = Map()
  var courierLocations : Map[Int, (Float, Float)] = Map()
  var courierOutOfFountain : Map[Int, Boolean] = Map()

  @OnEntityCreated(classPattern = "CDOTA_Unit_Hero_.*")
  def onHeroCreated(ctx: Context, e: Entity): Unit = {
    val isHero = e.hasProperty("m_hReplicatingOtherHeroModel") && e.getProperty[Int]("m_hReplicatingOtherHeroModel") == nullValue
    if (!isHero) return

    val playerId = e.getProperty[Int]("m_iPlayerID")
    val heroName = e.getDtClass.getDtName.replace("CDOTA_Unit_Hero_", "")

    heroMap += (playerId -> heroName)
  }

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Courier.*", propertyPattern = ".*m_cellX|.*_cellY")
  def onCourierLocationChanged(ctx: Context, e: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    if (gameStarted) return

    val (x, y) = (e.getProperty[Int]("CBodyComponent.m_cellX"), e.getProperty[Int]("CBodyComponent.m_cellY"))
    val (vecX, vecY) = (e.getProperty[Float]("CBodyComponent.m_vecX"), e.getProperty[Float]("CBodyComponent.m_vecY"))
    val playerId = e.getProperty[Int]("m_nPlayerOwnerID")

    courierLocations += (playerId -> (x * 128 + vecX - 8192, y * 128 + vecY - 8192))
  }

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_flGameStartTime")
  def onGameStartTimeChanged(ctx: Context, e: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val startTime = e.getPropertyForFieldPath[Float](fp)
    gameStarted = startTime > 1

    if (gameStarted)
      courierOutOfFountain = courierLocations.map(item => {
        val (playerId, location) = item
        val (x, y): (Float, Float) = location

        playerId -> isOutOfFountain(x, y)
      })
  }

  private def isOutOfFountain(x: Float, y: Float): Boolean = -x + 3600 < y && y < -x + 29080
}
