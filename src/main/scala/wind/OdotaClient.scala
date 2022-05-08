package wind

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sttp.client3.circe._
import sttp.client3.quick._
import wind.models.ReplayLocation

// todo make serialization not that shitty
case class Match(match_id: Long, match_seq_num: Long, radiant_win: Boolean, start_time: Long, duration: Long, avg_mmr: Option[Int], num_mmr: Option[Int], lobby_type: Int, game_mode: Int, avg_rank_tier: Int, num_rank_tier: Int, cluster: Int, radiant_team: String, dire_team: String)

object OdotaClient {
  private implicit val decodeReplayLocation: Decoder[ReplayLocation] =
    Decoder.forProduct3("match_id", "cluster", "replay_salt")(ReplayLocation.apply)

  private implicit val decodeMatch: Decoder[Match] = deriveDecoder

  def getReplayLocation(matchId: String): Option[ReplayLocation] = {
    val response = quickRequest
      .get(uri"https://api.opendota.com/api/replays?match_id=$matchId")
      .response(asJson[List[ReplayLocation]])
      .send(backend)

    response.body.toOption.flatMap(list => list.headOption)
  }

  def getPublicMatches(lessThanMatchId: String = ""): Option[List[Match]] = {
    val response = quickRequest
      .get(uri"https://api.opendota.com/api/publicMatches?mmr_descending=1&less_than_match_id=$lessThanMatchId")
      .response(asJson[List[Match]])
      .send(backend)

    response.body.toOption
  }
}
