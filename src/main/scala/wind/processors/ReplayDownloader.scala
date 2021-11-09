package wind.processors

import com.github.gekomad.scalacompress.Compressors._
import sttp.client3.quick._
import wind.{OdotaClient, ReplayLocation}

import java.io.File

object ReplayDownloader {
  def downloadReplay(matchId: String): Unit = {
    OdotaClient
      .getReplayLocation(matchId)
      .foreach(replayLocation => downloadReplay(replayLocation))
  }

  def downloadReplay(location: ReplayLocation): Unit = {
    println(s"Downloading replay for match ${location.matchId}...")
    val compressedFileName = "tmp/replay.dem.bz2"
    val replayArchive = new File(compressedFileName)
    val response = quickRequest
      .get(uri"http://replay${location.cluster}.valve.net/570/${location.matchId}_${location.salt}.dem.bz2")
      .response(asFile(replayArchive))
      .send(backend)

    if (!response.isSuccess) throw new Exception

    println("Replay downloaded, decompressing...")
    bzip2Decompress(compressedFileName, "tmp")
    println("Decompressed!")
  }
}
