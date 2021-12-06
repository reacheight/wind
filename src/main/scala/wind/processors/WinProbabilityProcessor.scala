package wind.processors

import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}
import skadistats.clarity.processor.runner.Context
import wind.{Util, WinProbabilityDataEntry}
import wind.Team._

import scala.collection.mutable.ArrayBuffer

class WinProbabilityProcessor {
  var data: ArrayBuffer[WinProbabilityDataEntry] = ArrayBuffer.empty

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

    val getNetworth: Entity => Seq[Int] = data => (0 to 4).map(player => data.getProperty[Int](s"m_vecDataTeam.000$player.m_iNetWorth"))
    val getExperience: Entity => Seq[Int] = data => (0 to 4).map(player => data.getProperty[Int](s"m_vecDataTeam.000$player.m_iTotalEarnedXP"))

    val networth = Map(Radiant -> getNetworth(radiantData), Dire -> getNetworth(direData))
    val experience = Map(Radiant -> getExperience(radiantData), Dire -> getExperience(direData))

    val (radiantTowers, direTowers) = Util.toList(entities.getAllByDtName("CDOTA_BaseNPC_Tower")).partition(tower => tower.getProperty[Int]("m_iTeamNum") == 2)
    val countTowers: List[Entity] => Seq[Int] = towers => (1 to 4).map(lvl => towers.count(t => t.getProperty[Int]("m_iCurrentLevel") == lvl))
    val towers = Map(Radiant -> countTowers(radiantTowers), Dire -> countTowers(direTowers))

    data += WinProbabilityDataEntry(gameTimeState.gameTime.toInt, networth, experience, towers)

    currentIteration += 1
  }
}
