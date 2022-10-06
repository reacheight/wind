package windota.processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.runner.Context
import windota.Util
import windota.extensions.FieldPath
import windota.extensions._
import windota.models.PlayerId
import windota.models.Role.Role

import scala.collection.mutable.ListBuffer

class ItemBuildProcessor(roles: Map[PlayerId, Role]) extends EntitiesProcessor {
  private var STICKS_CHECKED = false
  private val STICKS_CHECK_MINUTE = 3

  private val stickHeroes: List[String] = List("CDOTA_Unit_Hero_Batrider")
  private val stickItemNames = List("item_magic_stick", "item_magic_wand")

  private val _notPurchasedSticks: ListBuffer[(PlayerId, PlayerId)] = ListBuffer.empty //  (hero, stick hero)
  def notPurchasedSticks: Seq[(PlayerId, PlayerId)] = _notPurchasedSticks.toSeq

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_fGameTime")
  def onGameStartTimeChanged(ctx: Context, e: Entity, fp: FieldPath): Unit = {
    if (STICKS_CHECKED) return

    val gameTimeState = Util.getGameTimeState(e)

    if (gameTimeState.gameTime >= STICKS_CHECK_MINUTE * 60) {
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
              val itemUsageProcessor = ctx.getProcessor(classOf[ItemUsageProcessor])
              val items = itemUsageProcessor.getItems(oppHero)
              val hasStick = stickItemNames.map(name => itemUsageProcessor.findItem(items, name)).exists(_.isDefined)
              if (!hasStick)
                _notPurchasedSticks.addOne(Util.getPlayerId(oppHero), stickPlayerId)
            })
          }
        })
      })
    }
  }
}
