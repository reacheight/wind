package windota.processors

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import windota.extensions._
import windota.models.Team._

import scala.collection.mutable

class ScanProcessor {
  def scanUsageCount: Map[Team, Int] = _scanUsageCount.toMap

  private val _scanUsageCount: mutable.Map[Team, Int] = mutable.Map(Radiant -> 0, Dire -> 0)

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_fGoodRadarCooldown")
  def onRadiantScanUsed(gameRules: Entity, fp: FieldPath): Unit =
    _scanUsageCount(Radiant) += 1

  @OnEntityPropertyChanged(classPattern = "CDOTAGamerulesProxy", propertyPattern = "m_pGameRules.m_fBadRadarCooldown")
  def onDireScanUsed(gameRules: Entity, fp: FieldPath): Unit =
    _scanUsageCount(Dire) += 1
}
