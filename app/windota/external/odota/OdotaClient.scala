package windota.external.odota

import com.typesafe.scalalogging.Logger
import io.circe.{Decoder, HCursor}
import sttp.client3.circe.asJson
import sttp.client3.quick.backend
import sttp.client3.{UriContext, asString, basicRequest}
import windota.external.odota.models.{MatchReplayUrlResponse, RequestParsingResponse}

import scala.util.{Failure, Success, Try}

object OdotaClient {
  private val logger = Logger[OdotaClient.type]

  implicit val decodeReplayUrlResponse: Decoder[MatchReplayUrlResponse] = (c: HCursor) => {
    for {
      url <- c.downField("replay_url").as[Option[String]]
    } yield {
      MatchReplayUrlResponse(url)
    }
  }

  implicit val decodeRequestParsingResponse: Decoder[RequestParsingResponse] = (c: HCursor) => {
    for {
      jobId <- c.downField("job").downField("jobId").as[Long]
    } yield {
      RequestParsingResponse(jobId)
    }
  }
  
  def getMatchReplayUrl(matchId: Long): Try[Option[String]] = {
    logger.info(s"Getting match replay url of $matchId.")

    val response = basicRequest
      .get(uri"https://api.opendota.com/api/matches/$matchId")
      .response(asJson[MatchReplayUrlResponse])
      .send(backend)

    response.body.toTry.map(r => r.replayUrl)
  }

  def requestParsing(matchId: Long): Try[Long] = {
    logger.info(s"Requesting parsing of $matchId")

    val response = basicRequest
      .post(uri"https://api.opendota.com/api/request/$matchId")
      .response(asJson[RequestParsingResponse])
      .send(backend)

    response.body.toTry.map(r => r.jobId)
  }

  def isJobFinished(jobId: Long): Try[Boolean] = {
    logger.info(s"Checking if parsing job for $jobId is finished.")

    val response = basicRequest
      .get(uri"https://api.opendota.com/api/request/$jobId")
      .response(asString)
      .send(backend)

    response.body match {
      case Left(errorMessage) => Failure(new Exception(errorMessage))
      case Right(body) => Success(body == "null")
    }
  }
}
