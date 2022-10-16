package windota.external.stratz

import com.typesafe.scalalogging.Logger
import io.circe.{Decoder, HCursor}
import sttp.client3.circe.asJson
import sttp.client3.quick.backend
import sttp.client3.{UriContext, basicRequest}
import windota.external.stratz.decoders._
import windota.external.stratz.models._
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

    val query = s"{ player(steamAccountId: $accountId) { steamAccount { id name isAnonymous avatar } } }"

    val response = basicRequest
      .get(buildQueryUrl(query))
      .header("Authorization", authorizationToken)
      .response(asJson[User])
      .send(backend)

    response.body.toTry
  }

  def getMatches(accountId: Long, take: Int = 15, skip: Int = 0): Try[List[Match]] = {
    logger.info(s"Getting matches for user $accountId.")

    val query = s"{ player(steamAccountId: $accountId) { matches(request: { take: $take, skip: $skip }) { id durationSeconds didRadiantWin players { steamAccountId heroId isRadiant kills deaths assists } } } }"

    val response = basicRequest
      .get(buildQueryUrl(query))
      .header("Authorization", authorizationToken)
      .response(asJson[GetMatchesResult])
      .send(backend)

    response.body.toTry.map(result => result.matches)
  }

  def getMatch(matchId: Long): Try[Match] = {
    logger.info(s"Getting match $matchId.")

    val query = s"{ match(id: $matchId) { id durationSeconds didRadiantWin players { steamAccountId heroId isRadiant kills deaths assists } } }"

    val response = basicRequest
      .get(buildQueryUrl(query))
      .header("Authorization", authorizationToken)
      .response(asJson[GetMatchResult])
      .send(backend)

    response.body.toTry.map(result => result.`match`)
  }

  def getMatchItems(matches: Seq[Long]): Try[List[MatchItems]] = {
    logger.info(s"Getting match items for ${matches.mkString(", ")}.")

    val query = s"{ matches(ids: [${matches.mkString(", ")}]) { didRadiantWin, players { heroId, isRadiant, networth, item0Id, item1Id, item2Id, item3Id, item4Id, item5Id } } }"

    val response = basicRequest
      .get(buildQueryUrl(query))
      .header("Authorization", authorizationToken)
      .response(asJson[GetMatchItemsResult])
      .send(backend)

    response.body.toTry.map(r => r.matches)
  }

  private def buildQueryUrl(query: String) = uri"https://api.stratz.com/graphql?query=$query"
  private val authorizationToken = s"Bearer ${sys.env("STRATZ_TOKEN")}"
}