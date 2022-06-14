package wind.processors

import skadistats.clarity.processor.reader.OnMessage
import skadistats.clarity.wire.common.proto.DotaUserMessages._
import wind.extensions._
import wind.models.PlayerId

import scala.collection.mutable

class CursorProcessor extends EntitiesProcessor {
  val cursorItemDeliveryRangeY = 370 to 380
  val cursorItemDeliveryRangeX = (60 to 70).appendedAll(500 to 510)

  def mouseClicksItemDelivery = _mouseClicksItemDelivery.toSeq
  val _mouseClicksItemDelivery = mutable.Map.empty[PlayerId, Int].withDefaultValue(0)

  @OnMessage(classOf[CDOTAUserMsg_SpectatorPlayerUnitOrders])
  def onOrder(order: CDOTAUserMsg_SpectatorPlayerUnitOrders ): Unit = {
    if (order.getOrderType == 8) { // courier delivery (?)
      val units = toList(order.getUnitsList.iterator())
      val courierHierarchyId = units.head
      val courierMaybe = Entities.find(e => e.getDtClass.getDtName.startsWith("CDOTA_Unit_Courier") && e.hasProperty("m_nHierarchyId") && e.getProperty[Int]("m_nHierarchyId") == courierHierarchyId)
      courierMaybe.foreach(courier => {
        val playerId = courier.getProperty[Int]("m_nPlayerOwnerID")
        val playerController = Entities.getByPredicate(e => e.getDtClass.getDtName.startsWith("CDOTAPlayerController") && e.getProperty[Int]("m_nPlayerID") == playerId)
        val (cursorX, cursorY) = (playerController.getProperty[Int]("m_iCursor.0000"), playerController.getProperty[Int]("m_iCursor.0001"))
        if (cursorItemDeliveryRangeX.contains(cursorX) && cursorItemDeliveryRangeY.contains(cursorY))
          _mouseClicksItemDelivery.update(PlayerId(playerId), _mouseClicksItemDelivery(PlayerId(playerId)) + 1)
      })
    }
  }
}
