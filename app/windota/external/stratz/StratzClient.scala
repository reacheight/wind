package windota.external.stratz

import com.typesafe.scalalogging.Logger
import io.circe.{Decoder, HCursor}
import sttp.client3.circe.asJson
import sttp.client3.quick.backend
import sttp.client3.{UriContext, basicRequest}
import windota.external.stratz.decoders._
import windota.external.stratz.models.User
import windota.models.ReplayLocation

import scala.util.Try

object StratzClient {
  private val logger = Logger[StratzClient.type]

  private implicit val decodeReplayLocation: Decoder[ReplayLocation] = (c: HCursor) => {
    val matchData = c.downField("data").downField("match")
    for {
      matchId <- matchData.downField("id").as[Long]
      clusterId <- matchData.downField("clusterId").as[Long]
      salt <- matchData.downField("replaySalt").as[Long]
    } yield {
      ReplayLocation(matchId, clusterId, salt)
    }
  }

  def getReplayLocation(matchId: Long): Try[ReplayLocation] = {
    logger.info(s"Getting replay location from Stratz for match $matchId.")

    val query = s"{ match(id:$matchId) { id clusterId replaySalt } }"
    val response = basicRequest
      .get(buildQueryUrl(query))
      .header("Authorization", authorizationToken)
      .response(asJson[ReplayLocation])
      .send(backend)

    response.body.toTry
  }

  def getUser(accountId: Long): Try[User] = {
    logger.info(s"Getting user from Stratz for account $accountId.")

    val query = s"{ player(steamAccountId: $accountId) { steamAccount { id isAnonymous avatar } } }"

    val response = basicRequest
      .get(buildQueryUrl(query))
      .header("Authorization", authorizationToken)
      .response(asJson[User])
      .send(backend)

    response.body.toTry
  }

  private def buildQueryUrl(query: String) = uri"https://api.stratz.com/graphql?query=$query"
  private val authorizationToken = s"Bearer ${sys.env("STRATZ_TOKEN")}"
}