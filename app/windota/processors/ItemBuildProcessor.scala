package windota.processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.reader.OnMessage
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.NetworkBaseTypes
import windota.Util
import windota.Util.EntityExtension2
import windota.extensions.FieldPath
import windota.extensions._
import windota.models.{HeroId, PlayerId}
import windota.models.Role.{OffLane, Role}
import windota.models.Attribute._

import scala.collection.mutable.ListBuffer

class ItemBuildProcessor(roles: Map[PlayerId, Role]) extends ProcessorBase {
  private var STICKS_CHECKED = false
  private val STICKS_CHECK_MINUTE = 3

  private val stickHeroes: List[String] = List("CDOTA_Unit_Hero_Batrider", "CDOTA_Unit_Hero_Bristleback")
  private val stickItemNames = List("item_magic_stick", "item_magic_wand")

  private val heroItemData: List[(HeroId, String, String, String, Int, Int, Entity => Boolean)] = List(
    (HeroId(44), "CDOTA_Unit_Hero_PhantomAssassin", "item_monkey_king_bar", "Monkey King Bar", 48, 60, e => Util.isCoreRole(roles(Util.getPlayerId(e))) && roles(Util.getPlayerId(e)) != OffLane && e.primaryAttribute != Intelligence),
    (HeroId(10), "CDOTA_Unit_Hero_Morphling", "item_skadi", "Eye of Skadi", 45, 65, e => Util.isCoreRole(roles(Util.getPlayerId(e))) && roles(Util.getPlayerId(e)) != OffLane && e.primaryAttribute != Intelligence),
    (HeroId(99), "CDOTA_Unit_Hero_Bristleback", "item_silver_edge", "Silver Edge", 47, 57, e => Util.isCoreRole(roles(Util.getPlayerId(e))) && roles(Util.getPlayerId(e)) != OffLane && e.primaryAttribute != Intelligence),
  )

  private val _notPurchasedSticks: ListBuffer[(PlayerId, PlayerId)] = ListBuffer.empty //  (hero, stick hero)
  private val _notPurchasedItemAgainstHero: ListBuffer[(HeroId, String, Int, Int, Seq[PlayerId])] = ListBuffer.empty

  def notPurchasedSticks: Seq[(PlayerId, PlayerId)] = _notPurchasedSticks.toSeq
  def notPurchasedItemAgainstHero: Seq[(HeroId, String, Int, Int, Seq[PlayerId])] = _notPurchasedItemAgainstHero.toSeq

  @OnMessage(classOf[NetworkBaseTypes.CNETMsg_Tick])
  def onGameTimeChanged(ctx: Context, message: NetworkBaseTypes.CNETMsg_Tick): Unit = {
    if (STICKS_CHECKED) return

    if (GameTimeHelper.State.gameTime >= STICKS_CHECK_MINUTE * 60) {
      STICKS_CHECKED = true
      stickHeroes.foreach(stickHeroName => {
        val heroOpt = Entities.findByName(stickHeroName)
        heroOpt.foreach(hero => {
          val stickPlayerId = Util.getPlayerId(hero)
          val stickHeroRole = roles(stickPlayerId)
          val stickHeroTeam = Util.getTeam(hero)

          if (Util.isCoreRole(stickHeroRole)) {
            val oppRole = Util.getOppositeCoreRole(stickHeroRole)
            val oppTeam = Util.getOppositeTeam(stickHeroTeam)
            val oppHeroOpt = Entities.find(e => Util.isHero(e) && Util.getTeam(e) == oppTeam && roles(Util.getPlayerId(e)) == oppRole)

            oppHeroOpt.foreach(oppHero => {
              val items = ItemsHelper.getItems(oppHero)
              val hasStick = stickItemNames.map(name => ItemsHelper.findItem(items, name)).exists(_.isDefined)
              if (!hasStick)
                _notPurchasedSticks.addOne(Util.getPlayerId(oppHero), stickPlayerId)
            })
          }
        })
      })
    }
  }

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_nGameState")
  def onGameEnded(ctx: Context, gameRules: Entity, fp: FieldPath): Unit = {
    val gameState = gameRules.getPropertyForFieldPath[Int](fp)
    if (gameState != 6) return

    heroItemData.foreach { case (heroId, heroName, itemName, itemRealName, noItemWinrate, itemWinrate, candidatePredicate) =>
      val heroOpt = Entities.findByName(heroName)
      heroOpt.foreach(hero => {
        val heroTeam = Util.getTeam(hero)
        val candidatesForItem = Entities.filter(e => Util.isHero(e) && Util.getTeam(e) != heroTeam && candidatePredicate(e))
        val enemiesHasItem = candidatesForItem.exists(e => {
          val hasItem = ItemsHelper.findItem(hero, itemName).isDefined
          hasItem
        })

        if (!enemiesHasItem)
          _notPurchasedItemAgainstHero.addOne((heroId, itemRealName, noItemWinrate, itemWinrate, candidatesForItem.map(e => Util.getPlayerId(e))))
      })
    }
  }
}
