package windota.models

object AnalysisStatus extends Enumeration {
  type AnalysisStatus = Value
  val Processing, Finished, Failed = Value
}