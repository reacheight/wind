package windota.external.stratz

import io.circe.{Decoder, HCursor}
import windota.external.stratz.models._

package object decoders {
  implicit val decodeAvatarUrl: Decoder[User] = (c: HCursor) => {
    val steamAccount = c.downField("data").downField("player").downField("steamAccount")
    for {
      id <- steamAccount.downField("id").as[Long]
      isAnon <- steamAccount.downField("isAnonymous").as[Boolean]
      url <- steamAccount.downField("avatar").as[String]
    } yield {
      User(id, isAnon, url)
    }
  }
}
