package wind

import com.typesafe.scalalogging.Logger
import io.circe.generic.auto._
import sttp.client3.UriContext
import sttp.client3.circe.asJson
import sttp.client3.quick.{backend, quickRequest}
import wind.models.ReplayLocation

case class StratzMatch(clusterId: Long, replaySalt: Long)
case class ReplayData(`match`: StratzMatch)
case class ReplayResponse(data: ReplayData)

object StratzClient {
  val logger = Logger[StratzClient.type]

  def getReplayLocation(matchId: String): Option[ReplayLocation] = {
    logger.info(s"getting replay location from Stratz for $matchId")

    val response = quickRequest
      .get(uri"https://api.stratz.com/graphql?query={ match(id:$matchId) { clusterId replaySalt } }")
      .header("Authorization", s"Bearer ${sys.env("STRATZ_TOKEN")}")
      .response(asJson[ReplayResponse])
      .send(backend)

    response.body.toOption.map(response => ReplayLocation(matchId.toLong, response.data.`match`.clusterId, response.data.`match`.replaySalt))
  }
}
