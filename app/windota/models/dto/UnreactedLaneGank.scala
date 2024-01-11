package windota.models.dto

import windota.models.{GameTimeState, HeroId, PlayerId}
import windota.models.Lane.Lane

case class UnreactedLaneGank(target: HeroId, gankers: Seq[HeroId], gankTime: GameTimeState, deathTime: GameTimeState, lane: Lane)

object UnreactedLaneGank {
  def fromInternal(data: windota.models.internal.UnreactedLaneGank, heroId: PlayerId => HeroId): UnreactedLaneGank = {
    UnreactedLaneGank(heroId(data.target), data.gankers.map(heroId), data.gankTime, data.deathTime, data.lane)
  }
}
