package wind

import sttp.client3.UriContext
import sttp.client3.quick.{asFile, backend, quickRequest}

import java.io.File
import java.nio.file.Path

object ReplayDownloader {
  def downloadReplay(location: ReplayLocation, filePath: Path): Option[File] = {
    val response = quickRequest
      .get(uri"http://replay${location.cluster}.valve.net/570/${location.matchId}_${location.salt}.dem.bz2")
      .response(asFile(filePath.toFile))
      .send(backend)

    response.body.toOption
  }
}
