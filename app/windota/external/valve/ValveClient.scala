package windota.external.valve

import com.typesafe.scalalogging.Logger
import sttp.client3.UriContext
import sttp.client3.quick.{asFile, backend, quickRequest}
import windota.models.ReplayLocation

import java.io.{BufferedInputStream, File, FileInputStream}
import java.nio.file.{Files, Path, Paths}
import scala.util.{Failure, Success, Try, Using}

object ValveClient {
  private val logger = Logger[ValveClient.type]

  def downloadReplay(url: String, filePath: Path): Try[Unit] = {
    logger.info(s"Downloading replay for url $url.")

    val tmpFile = Paths.get(s"${filePath.toString}.tmp").toFile
    val response = quickRequest
      .get(uri"$url")
      .response(asFile(tmpFile))
      .send(backend)

    response.body match {
      case Right(tmpFile) =>
        saveFile(tmpFile, filePath)
        Success()
      case Left(string) => Failure(new Exception(string))
    }
  }

  def downloadReplay(location: ReplayLocation, filePath: Path): Try[Unit] = {
    logger.info(s"Downloading replay for ${location.matchId}.")

    val tmpFile = Paths.get(s"${filePath.toString}.tmp").toFile
    val response = quickRequest
      .get(getReplayUri(location))
      .response(asFile(tmpFile))
      .send(backend)

    response.body match {
      case Right(tmpFile) =>
        saveFile(tmpFile, filePath)
        Success()
      case Left(string) => Failure(new Exception(string))
    }
  }

  private def saveFile(file: File, path: Path) = {
    Using.Manager { use =>
      val inputStream = use(new BufferedInputStream(new FileInputStream(file)))
      val outputStream = use(Files.newOutputStream(path))
      inputStream.transferTo(outputStream)
    }
  }

  private def getReplayUri(location: ReplayLocation) = uri"http://replay${location.cluster}.valve.net/570/${location.matchId}_${location.salt}.dem.bz2"
}
