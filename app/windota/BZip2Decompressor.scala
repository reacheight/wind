package windota

import com.typesafe.scalalogging.Logger
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream

import java.io.BufferedInputStream
import java.nio.file.{Files, Path}
import scala.util.{Try, Using}

object BZip2Decompressor {
  private val logger = Logger[BZip2Decompressor.type]

  def decompress(compressed: Path, decompressed: Path): Try[Unit] = {
    logger.info(s"Decompressing $compressed to $decompressed.")
    Using.Manager { use =>
      val fileInput = use(Files.newInputStream(compressed))
      val bufferedInput = use(new BufferedInputStream(fileInput))
      val bzipInput = use(new BZip2CompressorInputStream(bufferedInput))

      val fileOutput = use(Files.newOutputStream(decompressed))

      fileOutput.write(bzipInput.readAllBytes())
    }
  }
}
