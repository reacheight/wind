package windota.utils

object SteamIdConverter {
  private val origin = 76561197960265728L

  def steam64toSteam3(steam64: Long): Long = steam64 - origin
  def steam3toSteam64(steam3: Long): Long = steam3 + origin
}
