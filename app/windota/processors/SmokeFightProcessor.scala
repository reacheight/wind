package windota.processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.reader.OnMessage
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.NetworkBaseTypes
import windota.Util
import windota.extensions._
import windota.models.Team._
import windota.models._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class SmokeFightProcessor(fights: Seq[Fight]) extends ProcessorBase {
  def smokeFights = _smokeFights.distinct.toSeq

  private val _smokeFights = ListBuffer.empty[(Map[Team, GameTimeState], Fight)]

  private val CHECK_HEROES_SMOKED = 5
  private val EPS = 0.05

  @OnMessage(classOf[NetworkBaseTypes.CNETMsg_Tick])
  def onGameTimeChanged(ctx: Context, message: NetworkBaseTypes.CNETMsg_Tick): Unit = {
    val gameTimeState = TimeState

    fights
      .find(fight => math.abs(fight.start.gameTime - gameTimeState.gameTime - CHECK_HEROES_SMOKED) < EPS)
      .foreach(fight => {
        val modifierProcessor = ctx.getProcessor(classOf[ModifierProcessor])
        val smoked = modifierProcessor.smokedHeroes

        val smokeTimes = mutable.Map.empty[Team, GameTimeState]

        val areRadiantSmoked = fight.radiantParticipants.forall(smoked.contains)
        if (areRadiantSmoked) {
          val radiantSmokeTime = fight.radiantParticipants.flatMap(smoked.get).head
          smokeTimes(Radiant) = radiantSmokeTime
        }

        val areDireSmoked = fight.direParticipants.forall(smoked.contains)
         if (areDireSmoked) {
          val direSmokeTime = fight.direParticipants.flatMap(smoked.get).head
           smokeTimes(Dire) = direSmokeTime
        }

        if (smokeTimes.nonEmpty)
          _smokeFights.addOne(smokeTimes.toMap, fight)
      })
  }
}
