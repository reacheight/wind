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

  implicit val decodeMatchItemsResult: Decoder[GetMatchItemsResult] = (c: HCursor) =>
    for {
      matches <- c.downField("data").downField("matches").as[List[MatchItems]]
    } yield {
      GetMatchItemsResult(matches)
    }

  implicit val decodeMatchItems: Decoder[MatchItems] = (c: HCursor) => {
    for {
      didRadiantWin <- c.downField("didRadiantWin").as[Boolean]
      analysisOutcome <- c.downField("analysisOutcome").as[String]
      players <- c.downField("players").as[List[PlayerItems]]
    } yield {
      MatchItems(didRadiantWin, analysisOutcome, players)
    }
  }

  implicit val decodePlayerItems: Decoder[PlayerItems] = (c: HCursor) => {
    for {
      heroId <- c.downField("heroId").as[Int]
      isRadiant <- c.downField("isRadiant").as[Boolean]
      networth <- c.downField("networth").as[Int]
      item0 <- c.downField("item0Id").as[Option[Int]]
      item1 <- c.downField("item1Id").as[Option[Int]]
      item2 <- c.downField("item2Id").as[Option[Int]]
      item3 <- c.downField("item3Id").as[Option[Int]]
      item4 <- c.downField("item4Id").as[Option[Int]]
      item5 <- c.downField("item5Id").as[Option[Int]]
    } yield {
      PlayerItems(heroId, isRadiant, networth, List(item0, item1, item2, item3, item4, item5).flatten)
    }
  }

  implicit val decodeHeroAbilitiesResult: Decoder[GetHeroAbilitiesResult] = (c: HCursor) => {
    for {
      abilities <- c.downField("data").downField("constants").downField("hero").downField("abilities").as[List[HeroAbility]]
    } yield {
      GetHeroAbilitiesResult(abilities)
    }
  }

  implicit val decodeHeroAbility: Decoder[HeroAbility] = (c: HCursor) => {
    val ability = c.downField("ability")
    val stat = ability.downField("stat")
    for {
      id <- c.downField("abilityId").as[Int]
      slot <- c.downField("slot").as[Int]
      displayName <- ability.downField("language").downField("displayName").as[String]
      isGrantedByShard <- stat.downField("isGrantedByShard").as[Boolean]
      isGrantedByScepter <- stat.downField("isGrantedByScepter").as[Boolean]
    } yield {
      HeroAbility(id, slot, displayName, isGrantedByShard, isGrantedByScepter)
    }
  }
}
