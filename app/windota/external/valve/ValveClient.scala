package windota.external.valve

import com.typesafe.scalalogging.Logger
import sttp.client3.UriContext
import sttp.client3.quick.{asFile, backend, quickRequest}
import windota.models.ReplayLocation

import java.io.{File, FileInputStream}
import java.nio.file.{Files, Path}
import scala.util.{Failure, Success, Try}

object ValveClient {
  private val logger = Logger[ValveClient.type]

  def downloadReplay(location: ReplayLocation, filePath: Path): Try[Unit] = {
    logger.info(s"Downloading replay for ${location.matchId}.")

    val response = quickRequest
      .get(getReplayUri(location))
      .response(asFile(filePath.toFile))
      .send(backend)

    response.body match {
      case Right(file) =>
        saveFile(file)
        Success()
      case Left(string) => Failure(new Exception(string))
    }
  }

  private def saveFile(file: File) = {
    file.createNewFile()
    val inputStream = new FileInputStream(file)
    Files.write(file.toPath, inputStream.readAllBytes())
  }

  private def getReplayUri(location: ReplayLocation) = uri"http://replay${location.cluster}.valve.net/570/${location.matchId}_${location.salt}.dem.bz2"
}
