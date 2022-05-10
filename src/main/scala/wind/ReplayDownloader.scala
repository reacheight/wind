package wind

import com.typesafe.scalalogging.Logger
import sttp.client3.UriContext
import sttp.client3.quick.{asFile, backend, quickRequest}
import wind.models.ReplayLocation

import java.io.File
import java.nio.file.Path

object ReplayDownloader {
  var logger = Logger[ReplayDownloader.type]

  def downloadReplay(location: ReplayLocation, filePath: Path): Option[File] = {
    logger.info(s"downloading replay for ${location.matchId}")

    val response = quickRequest
      .get(uri"http://replay${location.cluster}.valve.net/570/${location.matchId}_${location.salt}.dem.bz2")
      .response(asFile(filePath.toFile))
      .send(backend)

    response.body.toOption
  }
}
