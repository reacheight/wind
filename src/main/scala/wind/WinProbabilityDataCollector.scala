package wind

import skadistats.clarity.Clarity
import skadistats.clarity.processor.runner.SimpleRunner
import skadistats.clarity.source.MappedFileSource
import wind.processors.{HeroProcessor, WinProbabilityProcessor}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import scala.util.Using

object WinProbabilityDataCollector {
  private val directory = Paths.get("win_prob_data")

  def collect(replay: Path, matchId: String): Unit = {
    if (Files.exists(Paths.get(directory.toString, matchId)))
      return

    val gameInfo = Clarity.infoForFile(replay.toAbsolutePath.toString)
    val winProbabilityProcessor = new WinProbabilityProcessor
    val heroProcessor = new HeroProcessor(gameInfo)

    Using.Manager { use =>
      val source = use(new MappedFileSource(replay))(s => s.close())
      val runner = new SimpleRunner(source)
      runner.runWith(winProbabilityProcessor, heroProcessor)
    }

    val winner = gameInfo.getGameInfo.getDota.getGameWinner - 2
    val content = winProbabilityProcessor.data.map(_.toString + s" $winner").mkString("\n")
    Files.write(Paths.get(directory.toString, s"$matchId.txt"), content.getBytes(StandardCharsets.UTF_8))
  }
}
