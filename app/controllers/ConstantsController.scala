package controllers

import io.circe.generic.auto._
import io.circe.syntax._
import play.api.libs.circe.Circe
import play.api.mvc.{BaseController, ControllerComponents}
import windota.external.stratz.StratzClient
import windota.external.stratz.models.Item

import javax.inject.Inject
import scala.util.{Failure, Success}

class ConstantsController @Inject()(val controllerComponents: ControllerComponents) extends BaseController with Circe {
  def getHeroAbilities(heroId: Int) = Action {
    StratzClient.getHeroAbilities(heroId) match {
      case Failure(exception) => InternalServerError(exception.getMessage)
      case Success(abilities) => Ok(abilities.asJson)
    }
  }

  def getItems(itemsIds: String) = Action {
    if (itemsIds == "")
      Ok(List.empty[Item].asJson)
    else {
      val ids = itemsIds.split(",").map(s => s.toInt)
      val items = ids
        .map(id => StratzClient.getItem(id))
        .filter(tryItem => tryItem.isSuccess)
        .map(tryItem => tryItem.get)

      Ok(items.asJson)
    }
  }
}
