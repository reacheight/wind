package wind

import io.circe.Decoder
import sttp.client3.circe._
import sttp.client3.quick._

case class ReplayLocation(matchId: Long, cluster: Long, salt: Long)

object OdotaClient {
  private implicit val decodeReplayLocation: Decoder[ReplayLocation] =
    Decoder.forProduct3("match_id", "cluster", "replay_salt")(ReplayLocation.apply)

  def getReplayLocation(matchId: String): Option[ReplayLocation] = {
    val response = quickRequest
      .get(uri"https://api.opendota.com/api/replays?match_id=$matchId")
      .response(asJson[List[ReplayLocation]])
      .send(backend)

    response.body.toOption.flatMap(list => list.headOption)
  }
}
