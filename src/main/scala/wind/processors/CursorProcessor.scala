package wind.processors

import skadistats.clarity.model.{CombatLogEntry, Entity}
import skadistats.clarity.processor.entities.OnEntityCreated
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.reader.OnMessage
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages._
import wind.Util
import wind.extensions._
import wind.models.PlayerId

import scala.collection.mutable

class CursorProcessor extends EntitiesProcessor {
  private var combatLogHeroNameToPlayerId = Map[String, Int]()

  val cursorItemDeliveryRangeY = 370 to 380
  val cursorItemDeliveryRangeX = (60 to 70).appendedAll(500 to 510)

  val cursorQuickBuyRangeY = 337 to 347
  val cursorQuickBuyRangeX = (21 to 32).appendedAll(443 to 454)

  def mouseClicksItemDelivery = _mouseClicksItemDelivery.toSeq
  val _mouseClicksItemDelivery = mutable.Map.empty[PlayerId, Int].withDefaultValue(0)

  def mouseClicksQuickBuy = _mouseClicksQuickBuy.toSeq
  val _mouseClicksQuickBuy = mutable.Map.empty[PlayerId, Int].withDefaultValue(0)

  @OnEntityCreated(classPattern = "CWorld")
  def init(ctx: Context, e: Entity): Unit = {
    combatLogHeroNameToPlayerId = ctx.getProcessor(classOf[HeroProcessor]).combatLogNameToPlayerId
  }

  @OnMessage(classOf[CDOTAUserMsg_SpectatorPlayerUnitOrders])
  def onOrder(order: CDOTAUserMsg_SpectatorPlayerUnitOrders ): Unit = {
    if (order.getOrderType == 8) { // courier delivery (?)
      val units = toList(order.getUnitsList.iterator())
      val courierHierarchyId = units.head
      val courierMaybe = Entities.find(e => e.getDtClass.getDtName.startsWith("CDOTA_Unit_Courier") && e.hasProperty("m_nHierarchyId") && e.getProperty[Int]("m_nHierarchyId") == courierHierarchyId)
      courierMaybe.foreach(courier => {
        val playerId = courier.getProperty[Int]("m_nPlayerOwnerID")
        val (cursorX, cursorY) = getPlayerCursor(playerId)
        if (cursorItemDeliveryRangeX.contains(cursorX) && cursorItemDeliveryRangeY.contains(cursorY))
          _mouseClicksItemDelivery.update(PlayerId(playerId), _mouseClicksItemDelivery(PlayerId(playerId)) + 1)
      })
    }
  }

  @OnCombatLogEntry
  def onPurchase(cle: CombatLogEntry): Unit = {
    if (cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_PURCHASE) {
      val playerId = combatLogHeroNameToPlayerId(cle.getTargetName)
      val (cursorX, cursorY) = getPlayerCursor(playerId)
      if (cursorQuickBuyRangeX.contains(cursorX) && cursorQuickBuyRangeY.contains(cursorY))
        _mouseClicksQuickBuy.update(PlayerId(playerId), _mouseClicksQuickBuy(PlayerId(playerId)) + 1)
    }
  }

  def getPlayerCursor(id: Int): (Int, Int) = {
    val playerController = Entities.getByPredicate(e => e.getDtClass.getDtName.startsWith("CDOTAPlayerController") && e.getProperty[Int]("m_nPlayerID") == id)
    (playerController.getProperty[Int]("m_iCursor.0000"), playerController.getProperty[Int]("m_iCursor.0001"))
  }
}
