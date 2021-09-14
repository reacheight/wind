package wind

import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityCreated, OnEntityPropertyChanged}
import skadistats.clarity.processor.runner.Context

class CourierProcessor {
  private val nullValue = 16777215
  private val replicatingPropertyName = "m_hReplicatingOtherHeroModel"

  var heroMap : Map[Int, String] = Map()
  var courierOutOfFountain : Map[Int, Boolean] = Map()

  @OnEntityCreated(classPattern = "CDOTA_Unit_Hero_.*")
  def onHeroCreated(ctx: Context, e: Entity): Unit = {
    val isHero = e.hasProperty(replicatingPropertyName) && e.getProperty[Int](replicatingPropertyName) == nullValue
    if (!isHero) return

    val playerId = e.getProperty[Int]("m_iPlayerID")
    val heroName = e.getDtClass.getDtName.replace("CDOTA_Unit_Hero_", "")

    heroMap += (playerId -> heroName)
  }

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_flGameStartTime")
  def onGameStartTimeChanged(ctx: Context, e: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val startTime = e.getPropertyForFieldPath[Float](fp)

    if (startTime > 1) {
      val courierEntities = ctx.getProcessor(classOf[Entities]).getAllByPredicate(e => e.getDtClass.getDtName.startsWith("CDOTA_Unit_Courier"))
      courierEntities.forEachRemaining(courier => {
        val playerId = courier.getProperty[Int]("m_nPlayerOwnerID")
        val (x, y) = getCourierLocation(courier)

        courierOutOfFountain += (playerId -> isOutOfFountain(x, y))
      })
    }
  }

  private def getCourierLocation(courierEntity: Entity): (Float, Float) = {
    val (x, y) = (courierEntity.getProperty[Int]("CBodyComponent.m_cellX"), courierEntity.getProperty[Int]("CBodyComponent.m_cellY"))
    val (vecX, vecY) = (courierEntity.getProperty[Float]("CBodyComponent.m_vecX"), courierEntity.getProperty[Float]("CBodyComponent.m_vecY"))

    (x * 128 + vecX - 8192, y * 128 + vecY - 8192)
  }

  private def isOutOfFountain(x: Float, y: Float): Boolean = -x + 3600 < y && y < -x + 29080
}
