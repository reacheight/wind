package controllers

import io.circe.generic.auto._
import io.circe.syntax._
import play.api.libs.circe.Circe
import play.api.mvc.{BaseController, ControllerComponents}
import windota.external.stratz.StratzClient
import windota.external.stratz.encoders._
import windota.external.stratz.decoders._

import javax.inject.Inject
import scala.util.{Failure, Success}

class MatchesController @Inject()(val controllerComponents: ControllerComponents) extends BaseController with Circe {
  def getMatches(accountId: Long, page: Int) = Action {
    StratzClient.getMatches(accountId, skip = (page - 1) * 15) match {
      case Failure(exception) => InternalServerError(exception.getMessage)
      case Success(matches) => Ok(matches.asJson)
    }
  }

  def getMatch(matchId: Long) = Action {
    StratzClient.getMatch(matchId) match {
      case Failure(exception) => InternalServerError(exception.getMessage)
      case Success(dotaMatch) => Ok(dotaMatch.asJson)
    }
  }
}
