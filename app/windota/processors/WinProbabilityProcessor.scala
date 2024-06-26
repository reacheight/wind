package windota.processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}
import skadistats.clarity.processor.reader.OnMessage
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.NetworkBaseTypes
import windota.Util
import windota.extensions._
import windota.models.Team._
import windota.models.WinProbabilityDataEntry

import scala.collection.mutable.ListBuffer

class WinProbabilityProcessor extends ProcessorBase {
  var data: ListBuffer[WinProbabilityDataEntry] = ListBuffer.empty

  private val IterationInterval = 10
  private val Epsilon = 0.001f
  private var currentIteration = 1

  @OnMessage(classOf[NetworkBaseTypes.CNETMsg_Tick])
  def onGameTimeChanged(ctx: Context, message: NetworkBaseTypes.CNETMsg_Tick): Unit = {
    val gameTimeState = GameTimeHelper.State
    val gameRules = Entities.getByDtName("CDOTAGamerulesProxy")
    val gameOver = gameRules.getProperty[Int]("m_pGameRules.m_nGameState") > 5
    if (gameOver || !gameTimeState.preGameStarted || currentIteration * IterationInterval - gameTimeState.gameTime > Epsilon) return

    val entities = ctx.getProcessor(classOf[Entities])

    val radiantData = entities.getByDtName("CDOTA_DataRadiant")
    val direData = entities.getByDtName("CDOTA_DataDire")

    val networth = Map(Radiant -> getNetworth(radiantData), Dire -> getNetworth(direData))
    val experience = Map(Radiant -> getExperience(radiantData), Dire -> getExperience(direData))

    val (radiantTowers, direTowers) = entities.filterByName("CDOTA_BaseNPC_Tower").partition(tower => tower.getProperty[Int]("m_iTeamNum") == 2)
    val towers = Map(Radiant -> countTowers(radiantTowers), Dire -> countTowers(direTowers))

    val (radiantBarracks, direBarracks) = entities.filterByName("CDOTA_BaseNPC_Barracks").partition(tower => tower.getProperty[Int]("m_iTeamNum") == 2)
    val barracks = Map(Radiant -> countBarracks(radiantBarracks), Dire -> countBarracks(direBarracks))

    val heroProcessor = ctx.getProcessor(classOf[HeroProcessor])
    val radiantHeroes = Util.RadiantPlayerIds.map(id => entities.getByHandle(heroProcessor.heroHandle(id)))
    val direHeroes = Util.DirePlayerIds.map(id => entities.getByHandle(heroProcessor.heroHandle(id)))
    val respawnTime = Map(Radiant -> getSpawnTimes(radiantHeroes, gameTimeState.gameTime), Dire -> getSpawnTimes(direHeroes, gameTimeState.gameTime))

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
