package controllers

import io.circe.generic.auto._
import io.circe.syntax._
import play.api.Environment
import play.api.Mode.Dev
import play.api.libs.circe.Circe
import play.api.libs.openid.OpenIdClient
import play.api.mvc.{AnyContent, BaseController, ControllerComponents, Request}
import windota.external.stratz.StratzClient

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util._

class AccountController @Inject()(val openIdClient: OpenIdClient, val controllerComponents: ControllerComponents, val environment: Environment) extends BaseController with Circe {
  val steamOpenId = "https://steamcommunity.com/openid/"
  val loginCallbackRedirect = if (environment.mode == Dev) "http://localhost:3000" else "https://windota.xyz"

  def login = Action.async { implicit request =>
    openIdClient.redirectURL(steamOpenId, routes.AccountController.loginCallback.absoluteURL())
      .map(url => Redirect(url, status=FOUND))
  }

  def loginCallback = Action.async { implicit request: Request[AnyContent] =>
    openIdClient
      .verifiedId(request)
      .map(info => Redirect(loginCallbackRedirect).withSession("id" -> info.id.split('/').last))
  }

  def getUser = Action { request =>
    request.session.get("id") match {
      case None => Unauthorized
      case Some(id) =>
        val accountId = id.toLong - 76561197960265728L
        StratzClient.getUser(accountId) match {
          case Failure(exception) => InternalServerError(exception.getMessage)
          case Success(user) => Ok(user.asJson)
        }
    }
  }
}
