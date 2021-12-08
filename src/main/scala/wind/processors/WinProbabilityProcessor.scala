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

    val networth = Map(Radiant -> getNetworth(radiantData), Dire -> getNetworth(direData))
    val experience = Map(Radiant -> getExperience(radiantData), Dire -> getExperience(direData))

    val (radiantTowers, direTowers) = Util.toList(entities.getAllByDtName("CDOTA_BaseNPC_Tower")).partition(tower => tower.getProperty[Int]("m_iTeamNum") == 2)
    val towers = Map(Radiant -> countTowers(radiantTowers), Dire -> countTowers(direTowers))

    val (radiantBarracks, direBarracks) = Util.toList(entities.getAllByDtName("CDOTA_BaseNPC_Barracks")).partition(tower => tower.getProperty[Int]("m_iTeamNum") == 2)
    val barracks = Map(Radiant -> countBarracks(radiantBarracks), Dire -> countBarracks(direBarracks))

    val heroProcessor = ctx.getProcessor(classOf[HeroProcessor])
    val radiantHeroes = (0 to 4).map(id => entities.getByHandle(heroProcessor.heroHandleMap(id)))
    val direHeroes = (5 to 9).map(id => entities.getByHandle(heroProcessor.heroHandleMap(id)))
    val isAlive = Map( Radiant -> getLifeStates(radiantHeroes), Dire -> getLifeStates(direHeroes))

    data += WinProbabilityDataEntry(gameTimeState.gameTime.toInt, networth, experience, towers, barracks, isAlive)

    currentIteration += 1
  }

  private def getNetworth(dataEntity: Entity): Seq[Int] = (0 to 4).map(player => dataEntity.getProperty[Int](s"m_vecDataTeam.000$player.m_iNetWorth"))

  private def getExperience(dataEntity: Entity): Seq[Int] = (0 to 4).map(player => dataEntity.getProperty[Int](s"m_vecDataTeam.000$player.m_iTotalEarnedXP"))

  private def countTowers(towers: Seq[Entity]): Seq[Int] = (1 to 4).map(lvl => towers.count(t => t.getProperty[Int]("m_iCurrentLevel") == lvl))

  private def countBarracks(barracks: Seq[Entity]): Seq[Int] = Seq(1300, 2200).map(hp => barracks.count(b => b.getProperty[Int]("m_iMaxHealth") == hp))

  private def getLifeStates(heroes: Seq[Entity]): Seq[Boolean] = heroes.map(hero => hero.getProperty[Int]("m_lifeState") == 0)
}
