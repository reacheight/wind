package windota.external.stratz

import io.circe.{Decoder, HCursor}
import io.circe.generic.auto._
import windota.external.stratz.models._

package object decoders {
  implicit val decodeAvatarUrl: Decoder[User] = (c: HCursor) => {
    val steamAccount = c.downField("data").downField("player").downField("steamAccount")
    for {
      id <- steamAccount.downField("id").as[Long]
      name <- steamAccount.downField("name").as[String]
      isAnon <- steamAccount.downField("isAnonymous").as[Boolean]
      url <- steamAccount.downField("avatar").as[String]
    } yield {
      User(id, name, isAnon, url)
    }
  }

  implicit val decodeMatches: Decoder[GetMatchesResult] = (c: HCursor) =>
    for {
      matches <- c.downField("data").downField("player").downField("matches").as[List[Match]]
    } yield {
      GetMatchesResult(matches)
    }

  implicit val decodeMatch: Decoder[GetMatchResult] = (c: HCursor) =>
    for {
      dotaMatch <- c.downField("data").downField("match").as[Match]
    } yield {
      GetMatchResult(dotaMatch)
    }
}
