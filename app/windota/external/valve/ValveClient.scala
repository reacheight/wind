package windota.external.valve

import com.typesafe.scalalogging.Logger
import sttp.client3.UriContext
import sttp.client3.quick.{asFile, backend, quickRequest}
import windota.models.ReplayLocation

import java.nio.file.Path
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
      case Right(_) => Success()
      case Left(string) => Failure(new Exception(string))
    }
  }

  private def getReplayUri(location: ReplayLocation) = uri"http://replay${location.cluster}.valve.net/570/${location.matchId}_${location.salt}.dem.bz2"
}
