package windota.processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.runner.Context
import windota.Util
import windota.extensions._
import windota.models.ItemAgainstHeroDataEntry
import windota.models.Team._

class ItemsAgainstHeroProcessor extends ProcessorBase {
  val HERO_NAME = "CDOTA_Unit_Hero_PhantomAssassin"
  val ITEM_NAME = "item_monkey_king_bar"

  private var _dataEntry: Option[ItemAgainstHeroDataEntry] = None
  def result: Option[ItemAgainstHeroDataEntry] = _dataEntry

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_nGameState")
  def onGameEnded(ctx: Context, gameRules: Entity, fp: FieldPath): Unit = {
    val gameState = gameRules.getPropertyForFieldPath[Int](fp)
    if (gameState != 6) return

    val itemUsageProcessor = ctx.getProcessor(classOf[ItemUsageProcessor])
    def heroesHasItem(heroes: Seq[Entity]): Boolean = heroes.exists(h => {
      val items = itemUsageProcessor.getItems(h)
      val item = itemUsageProcessor.findItem(items, ITEM_NAME)
      item.nonEmpty
    })

    val hero = Entities.getByDtName(HERO_NAME)
    val heroTeam = Util.getTeam(hero)
    val isHeroRadiant = heroTeam == Radiant

    val radiant = Entities.filter(e => Util.isHero(e) && Util.getTeam(e) == Radiant)
    val radiantHasItem = heroesHasItem(radiant)

    val dire = Entities.filter(e => Util.isHero(e) && Util.getTeam(e) == Dire)
    val direHasItem = heroesHasItem(dire)

    val winner = gameRules.getProperty[Integer]("m_pGameRules.m_nGameWinner") // 2 - radiant, 3 - dire
    val radiantWon = winner == 2

    _dataEntry = Some(ItemAgainstHeroDataEntry(isHeroRadiant, radiantHasItem, direHasItem, 0, 0, radiantWon, false))
  }
}
