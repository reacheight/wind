package wind.processors

import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}
import skadistats.clarity.processor.runner.Context
import wind.Util

class WinProbabilityProcessor {
  var networth: Map[Float, (Int, Int)] = Map()
  var experience: Map[Float, (Int, Int)] = Map()

  private val IterationInterval = 60
  private val Epsilon = 0.001f
  private var currentIteration = 1

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_fGameTime")
  def onGameTimeChanged(ctx: Context, gameRules: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameTimeState = Util.getGameTimeState(gameRules)
    if (!gameTimeState.preGameStarted || currentIteration * IterationInterval - gameTimeState.gameTime > Epsilon) return

    val entities = ctx.getProcessor(classOf[Entities])
    val radiantData = entities.getByDtName("CDOTA_DataRadiant")
    val direData = entities.getByDtName("CDOTA_DataDire")

    val getNetworth: Entity => Int = data => (0 to 4).map(player => data.getProperty[Int](s"m_vecDataTeam.000$player.m_iNetWorth")).sum
    networth += gameTimeState.gameTime -> (getNetworth(radiantData), getNetworth(direData))

    val getExperience: Entity => Int = data => (0 to 4).map(player => data.getProperty[Int](s"m_vecDataTeam.000$player.m_iTotalEarnedXP")).sum
    experience += gameTimeState.gameTime -> (getExperience(radiantData), getExperience(direData))

    currentIteration += 1
  }
}
