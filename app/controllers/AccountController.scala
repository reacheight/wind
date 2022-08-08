package controllers

import play.api.Environment
import play.api.Mode.Dev
import play.api.libs.openid.OpenIdClient
import play.api.mvc.{AnyContent, BaseController, ControllerComponents, Request}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

class AccountController @Inject()(val openIdClient: OpenIdClient, val controllerComponents: ControllerComponents, val environment: Environment) extends BaseController {
  val steamOpenId = "https://steamcommunity.com/openid/"
  val loginCallbackRedirect = if (environment.mode == Dev) "http://localhost:3000" else "https://windota.xyz"

  def login = Action.async { implicit request =>
    openIdClient.redirectURL(steamOpenId, routes.AccountController.loginCallback.absoluteURL())
      .map(url => Redirect(url, status=FOUND))
  }

  def loginCallback = Action.async { implicit request: Request[AnyContent] =>
    openIdClient
      .verifiedId(request)
      .map(info => Redirect(loginCallbackRedirect).withSession("id" -> info.id))
  }
}
