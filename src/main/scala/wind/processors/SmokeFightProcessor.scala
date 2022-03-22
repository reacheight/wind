package wind.processors

import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.runner.Context
import wind.Util
import wind.models.{Fight, GameTimeState}

import scala.collection.mutable.ListBuffer

class SmokeFightProcessor(fights: Seq[Fight]) extends EntitiesProcessor {
  def smokeFights = _smokeFights.distinct.toSeq

  private val _smokeFights = ListBuffer.empty[GameTimeState]

  private val CHECK_HEROES_SMOKED = 5
  private val EPS = 0.05

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_fGameTime")
  def onGameTimeChanged(ctx: Context, gameRulesEntity: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameTimeState = Util.getGameTimeState(gameRulesEntity)

    fights
      .find(fight => math.abs(fight.start.gameTime - gameTimeState.gameTime - CHECK_HEROES_SMOKED) < EPS)
      .foreach(fight => {
        val modifierProcessor = ctx.getProcessor(classOf[ModifierProcessor])
        val smoked = modifierProcessor.smokedHeroes

        val areRadiantSmoked = fight.radiantParticipants.forall(smoked.contains)
        val areDireSmoked = fight.direParticipants.forall(smoked.contains)

        if (areRadiantSmoked || areDireSmoked)
          _smokeFights.addOne(fight.start)
      })
  }
}
