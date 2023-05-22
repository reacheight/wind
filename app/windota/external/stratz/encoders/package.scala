package windota.external.stratz

import io.circe.Encoder
import io.circe.syntax._
import windota.external.stratz.models.Lane.Lane
import windota.external.stratz.models.Position.Position

package object encoders {
  implicit val fromPosition: Encoder[Position] = (pos: Position) => pos.id.asJson
  implicit val fromLane: Encoder[Lane] = (lane: Lane) => lane.id.asJson
}
