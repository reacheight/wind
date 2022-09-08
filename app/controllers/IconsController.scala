package controllers

import play.api.mvc.{BaseController, ControllerComponents}
import windota.constants.Heroes

import java.nio.file.{Files, Paths}
import javax.inject.Inject

class IconsController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  def getIcon(heroId: Int) = Action {
    val heroTag = Heroes.getTag(heroId)
    val imagePath = Paths.get(s"conf/icons/$heroTag.png")
    val image = Files.readAllBytes(imagePath)
    Ok(image).as("image/png")
  }

  def getMiniPortrait(heroId: Int) = Action {
    val heroTag = Heroes.getTag(heroId)
    val imagePath = Paths.get(s"conf/miniportraits/$heroTag.png")
    val image = Files.readAllBytes(imagePath)
    Ok(image).as("image/png")
  }
}
