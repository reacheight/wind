package wind.processors

import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}
import skadistats.clarity.processor.runner.Context
import wind.{Util, WinProbabilityDataEntry}
import wind.models.Team._

import scala.collection.mutable.ArrayBuffer

class WinProbabilityProcessor {
  var data: ArrayBuffer[WinProbabilityDataEntry] = ArrayBuffer.empty

  private val IterationInterval = 10
  private val Epsilon = 0.001f
  private var currentIteration = 1

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy.*", propertyPattern = "m_pGameRules.m_fGameTime")
  def onGameTimeChanged(ctx: Context, gameRules: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    val gameTimeState = Util.getGameTimeState(gameRules)
    val gameOver = gameRules.getProperty[Int]("m_pGameRules.m_nGameState") > 5
    if (gameOver || !gameTimeState.preGameStarted || currentIteration * IterationInterval - gameTimeState.gameTime > Epsilon) return

    val entities = ctx.getProcessor(classOf[Entities])

    val radiantData = entities.getByDtName("CDOTA_DataRadiant")
    val direData = entities.getByDtName("CDOTA_DataDire")

    val networth = Map(Radiant -> getNetworth(radiantData), Dire -> getNetworth(direData))
    val experience = Map(Radiant -> getExperience(radiantData), Dire -> getExperience(direData))

    val (radiantTowers, direTowers) = Util.toList(entities.getAllByDtName("CDOTA_BaseNPC_Tower")).partition(tower => tower.getProperty[Int]("m_iTeamNum") == 2)
    val towers = Map(Radiant -> countTowers(radiantTowers), Dire -> countTowers(direTowers))

    val (radiantBarracks, direBarracks) = Util.toList(entities.getAllByDtName("CDOTA_BaseNPC_Barracks")).partition(tower => tower.getProperty[Int]("m_iTeamNum") == 2)
    val barracks = Map(Radiant -> countBarracks(radiantBarracks), Dire -> countBarracks(direBarracks))

    val time = gameRules.getProperty[Float]("m_pGameRules.m_fGameTime")
    val heroProcessor = ctx.getProcessor(classOf[HeroProcessor])
    val radiantHeroes = Util.RadiantPlayerIds.map(id => entities.getByHandle(heroProcessor.heroHandle(id)))
    val direHeroes = Util.DirePlayerIds.map(id => entities.getByHandle(heroProcessor.heroHandle(id)))
    val respawnTime = Map(Radiant -> getSpawnTimes(radiantHeroes, time), Dire -> getSpawnTimes(direHeroes, time))

    val buybackState = Map(
      Radiant -> getBuybackStates(radiantData, networth(Radiant), gameTimeState.gameTime),
      Dire-> getBuybackStates(direData, networth(Dire), gameTimeState.gameTime)
    )

    data += WinProbabilityDataEntry(gameTimeState.gameTime.toInt, networth, experience, towers, barracks, respawnTime, buybackState)

    currentIteration += 1
  }

  private def getNetworth(dataEntity: Entity): IndexedSeq[Int] = (0 to 4).map(player => dataEntity.getProperty[Int](s"m_vecDataTeam.000$player.m_iNetWorth"))

  private def getExperience(dataEntity: Entity): IndexedSeq[Int] = (0 to 4).map(player => dataEntity.getProperty[Int](s"m_vecDataTeam.000$player.m_iTotalEarnedXP"))

  private def countTowers(towers: Seq[Entity]): IndexedSeq[Int] = (1 to 4).map(lvl => towers.count(t => t.getProperty[Int]("m_iCurrentLevel") == lvl))

  private def countBarracks(barracks: Seq[Entity]): Seq[Int] = Seq(1300, 2200).map(hp => barracks.count(b => b.getProperty[Int]("m_iMaxHealth") == hp))

  private def getSpawnTimes(heroes: Seq[Entity], time: Float): Seq[Float] = heroes.map(hero => Util.getSpawnTime(hero, time))

  private def getBuybackStates(dataEntity: Entity, networth: IndexedSeq[Int], time: Float): Seq[Boolean] = (0 to 4).map(player => {
    val buybackCooldown = dataEntity.getProperty[Float](s"m_vecDataTeam.000$player.m_flBuybackCooldownTime")
    val gold = dataEntity.getProperty[Int](s"m_vecDataTeam.000$player.m_iReliableGold") + dataEntity.getProperty[Int](s"m_vecDataTeam.000$player.m_iUnreliableGold")
    val buybackCost = 200 + math.floor(networth(player) / 13)
    buybackCooldown < time && buybackCost < gold
  })
}
