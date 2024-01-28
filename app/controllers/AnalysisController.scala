package controllers

import com.typesafe.scalalogging.Logger
import io.circe.syntax._
import play.api.libs.circe.Circe
import play.api.mvc.{BaseController, ControllerComponents}
import windota.converters._
import windota.external.odota.OdotaClient
import windota.external.valve.ValveClient
import windota.models.AnalysisStatus
import windota.{BZip2Decompressor, MongoClient, ReplayAnalyzer}

import java.nio.file.{Files, Path, Paths}
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class AnalysisController @Inject()(val controllerComponents: ControllerComponents) extends BaseController with Circe {
  private val logger = Logger[AnalysisController]

  private val DownloadingDirectory = "replays"
  private def compressedReplayPath(matchId: Long) = Paths.get(DownloadingDirectory, s"${matchId}_compressed")
  private def replayPath(matchId: Long) = Paths.get(DownloadingDirectory, s"$matchId.dem")

  def postAnalysis(matchId: Long) = Action.async {
    MongoClient.getStatus(matchId).flatMap {
      case Some(status) if status != AnalysisStatus.Failed => Future.successful(Created)
      case _ =>
        MongoClient.setStatus(matchId, AnalysisStatus.Processing).map(_ => {
          Future { startAnalysis(matchId) }
          Created
        })
    }
  }

  def postAnalysisFile(matchId: Long) = Action(parse.multipartFormData).async { request =>
    MongoClient.getStatus(matchId).flatMap {
      case Some(status) if status != AnalysisStatus.Failed => Future.successful(Created)
      case _ =>
        request.body
          .file("replay")
          .map { replay =>
            replay.ref.copyTo(replayPath(matchId), true)
            if (!ReplayAnalyzer.isMatchReplay(matchId, replayPath(matchId))) {
              Future.successful(BadRequest("WRONG_MATCH"))
            } else {
              MongoClient.setStatus(matchId, AnalysisStatus.Processing).map(_ => {
                Future { startAnalysis(matchId, replayPath(matchId)) }
                Created
              })
            }
          }.getOrElse {
            Future.successful(BadRequest)
          }
    }
  }

  def getAnalysisState(matchId: Long) = Action.async {
    MongoClient.getStatus(matchId) map {
      case Some(state) => Ok(state.asJson)
      case None => NotFound
    }
  }

  def getAnalysis(matchId: Long) = Action.async {
    MongoClient.getAnalysisJson(matchId) map {
      case Some(json) => Ok(json.asJson)
      case None => NotFound
    }
  }

  def getAnalysisCount = Action.async {
    MongoClient.getAnalysisCount.map(count => Ok(count.toString))
  }

  private def startAnalysis(matchId: Long): Unit = {
    if (!Files.exists(Paths.get(DownloadingDirectory)))
      Files.createDirectory(Paths.get(DownloadingDirectory))

    val replayUrlOpt = OdotaClient.requestParsing(matchId).toOption
      .flatMap(jobId => getReplayUrlWithMultipleAttempts(matchId, jobId, 3))

//    val path = s"D:\\SteamLibrary\\steamapps\\common\\dota 2 beta\\game\\dota\\replays\\$matchId.dem"
//    val analysis = ReplayAnalyzer.analyze(Paths.get(path))
//    MongoClient.saveAnalysis(analysis)
//      .flatMap(_ => MongoClient.setStatus(matchId, AnalysisStatus.Finished))

    replayUrlOpt match {
      case None =>
        MongoClient.setStatus(matchId, AnalysisStatus.Failed)
        logger.error(s"Couldn't get replay url of $matchId by OpenDota.")
      case Some(url) =>
        val replayTry = ValveClient.downloadReplay(url, compressedReplayPath(matchId))
          .flatMap(_ => BZip2Decompressor.decompress(compressedReplayPath(matchId), replayPath(matchId)))

        replayTry match {
          case Failure(e) =>
            MongoClient.setStatus(matchId, AnalysisStatus.Failed)
            logger.error(s"Failed to get replay of $matchId: ${e.toString}.")
          case Success(_) =>
            val analysis = ReplayAnalyzer.analyze(replayPath(matchId))
            Future { Paths.get(DownloadingDirectory).toFile.listFiles().foreach(_.delete()) }
            MongoClient.saveAnalysis(analysis)
              .flatMap(_ => MongoClient.setStatus(matchId, AnalysisStatus.Finished))
        }
    }
  }
  private def getReplayUrlWithMultipleAttempts(matchId: Long, jobId: Long, attempts: Int): Option[String] = {
    for (i <- 1 to attempts) {
      Thread.sleep(10000)
      getReplayUrlIfJobIsFinished(matchId, jobId)
        .foreach(url => return Some(url))
    }

    None
  }

  private def getReplayUrlIfJobIsFinished(matchId: Long, jobId: Long): Option[String] = {
    OdotaClient.isJobFinished(jobId).toOption.flatMap(isJobFinished => {
      if (isJobFinished)
        OdotaClient.getMatchReplayUrl(matchId).toOption.flatten
      else
        None
    })
  }

  private def startAnalysis(matchId: Long, path: Path) = {
    logger.info(s"startAnalysis in AnalysisController for matchId = $matchId.")

    try {
      val analysis = ReplayAnalyzer.analyze(path)
      Future { Paths.get(DownloadingDirectory).toFile.listFiles().foreach(_.delete()) }
      MongoClient.saveAnalysis(analysis)
        .flatMap(_ => MongoClient.setStatus(matchId, AnalysisStatus.Finished))
    } catch {
      case e => logger.error(s"${e.getMessage}\n${e.getStackTrace.mkString("\n")}")
    }
  }
}
