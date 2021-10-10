package wind
package processors

import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{OnEntityCreated, OnEntityPropertyChanged}
import skadistats.clarity.processor.runner.Context

class SummonsProcessor {
  var summonFeedGold: Map[Int, Int] = Map()
  private var summons = Set[Int]()

  @OnEntityCreated()
  def onSummonCreated(ctx: Context, summon: Entity): Unit = {
    val isSummoned = summon.hasProperty("m_bIsSummoned") && summon.getProperty[Boolean]("m_bIsSummoned")
    if (!isSummoned) return

    val playerId = summon.getProperty[Int]("m_nPlayerOwnerID")
    if (playerId < 0) return

    summons += summon.getHandle
  }

  @OnEntityPropertyChanged(propertyPattern = "m_lifeState")
  def onSummonLifeStateChanged(ctx: Context, summon: Entity,  fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    if (!summons.contains(summon.getHandle)) return

    val lifeState = summon.getProperty[Int]("m_lifeState")
    if (lifeState != 2) return

    val playerId = summon.getProperty[Int]("m_nPlayerOwnerID")
    val gold = summon.getProperty[Int]("m_iGoldBountyMin")

    // todo: check if unit was actually killed
    if (summonFeedGold.contains(playerId))
      summonFeedGold += playerId -> (summonFeedGold(playerId) + gold)
    else
      summonFeedGold += playerId -> gold

    summons -= summon.getHandle
  }
}
