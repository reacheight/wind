package windota

import com.typesafe.scalalogging.Logger
import skadistats.clarity.processor.runner.SimpleRunner
import skadistats.clarity.source.MappedFileSource
import windota.external.stratz.StratzClient
import windota.external.valve.ValveClient
import windota.models.ItemAgainstHeroDataEntry
import windota.processors.{ItemUsageProcessor, ItemsAgainstHeroProcessor}

import java.io.FileWriter
import java.nio.file.Paths
import scala.util.{Failure, Success, Using}

object ItemAgainstHeroRunner {
  private val logger = Logger[ItemAgainstHeroRunner.type]
  private val resultFile = Paths.get("items_against_hero_stats", "necr_vessel_nw.txt").toFile

  def runWithProcessor(matches: Seq[Long]): Unit = {
    val itemUsageProcessor = new ItemUsageProcessor(false)
    val fw = new FileWriter(resultFile, true)

    matches.zip(1 to matches.length).foreach { case (matchId, idx) =>
      try {
        val replayLocationOpt = StratzClient.getReplayLocation(matchId)
        replayLocationOpt.map(replayLocation => {
          val replayPathCompressed = Paths.get("items_compressed", matchId.toString)
          val replayPath = Paths.get("items", matchId.toString)
          ValveClient.downloadReplay(replayLocation, replayPathCompressed).map(_ => {
            BZip2Decompressor.decompress(replayPathCompressed, replayPath).map(_ => {
              Using.Manager { use =>
                logger.info(s"Running ItemAgainstHeroProcessor for $matchId.")
                val source = use(new MappedFileSource(replayPath))(s => s.close())
                val runner = new SimpleRunner(source)
                val itemsAgainstHeroProcessor = new ItemsAgainstHeroProcessor

                try {
                  runner.runWith(itemUsageProcessor, itemsAgainstHeroProcessor)
                } catch {
                  case e => logger.error(s"Error for $matchId: ${e.getMessage}\n${e.getStackTrace.mkString("\n")}")
                }

                itemsAgainstHeroProcessor.result.foreach(dataEntry => {
                  fw.write(dataEntry.toString + "\n")
                  fw.flush()
                  logger.info(s"Success for match #$idx ($matchId): ${dataEntry.toString}")
                })
              }

              replayPathCompressed.toFile.delete()
              replayPath.toFile.delete()
            })
          })
        })
      } catch {
        case e => logger.error(s"Error for $matchId: ${e.getMessage}\n${e.getStackTrace.mkString("\n")}")
      }
    }

    fw.close()
  }

  def runWithStratz(matches: Seq[Long]): Unit = {
    val HERO_ID = 36
    val ITEM_ID = 267

    val fw = new FileWriter(resultFile, true)

    val matchGroups = matches.grouped(10).toList
    matchGroups.zip(1 to matchGroups.length).foreach { case (group, groupNumber) =>
      try {
        StratzClient.getMatchItems(group) match {
          case Failure(e) => logger.error(s"Error for group number #$groupNumber (firstMatch is ${group.head}): ${e.getMessage}\n${e.getStackTrace.mkString("\n")}")
          case Success(matchItemsList) =>
            matchItemsList.foreach(matchItems => {
              val hero = matchItems.players.find(p => p.heroId == HERO_ID).get
              val radiantHasItem = matchItems.radiant.exists(p => p.items.contains(ITEM_ID))
              val direHasItem = matchItems.dire.exists(p => p.items.contains(ITEM_ID))
              val radiantNetworth = matchItems.radiant.map(p => p.networth).sum
              val direNetworth = matchItems.dire.map(p => p.networth).sum
              val dataEntry = ItemAgainstHeroDataEntry(hero.isRadiant, radiantHasItem, direHasItem, radiantNetworth, direNetworth, matchItems.didRadiantWin)

              fw.write(dataEntry.toString + "\n")
              fw.flush()
            })

            logger.info(s"Success for match group number #$groupNumber (firstMatch is ${group.head}).")
            Thread.sleep(1000)
        }
      } catch {
        case e => logger.error(s"Error for match group number #$groupNumber (firstMatch is ${group.head}): ${e.getMessage}\n${e.getStackTrace.mkString("\n")}")
      }
    }

    fw.close()
  }
}
