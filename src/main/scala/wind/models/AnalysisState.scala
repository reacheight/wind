package wind.models

object AnalysisStatus extends Enumeration {
  type AnalysisStatus = Value
  val Processing, Finished, Failed = Value
}

case class AnalysisState(matchId: Long, status: AnalysisStatus.AnalysisStatus)