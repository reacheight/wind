package wind

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream

import java.io.BufferedOutputStream
import java.nio.file.{Files, Path}
import scala.util.Using

object BZip2Decompressor {
  def decompress(compressed: Path, decompressed: Path): Unit = {
    Using.Manager { use =>
      val fileInput = use(Files.newInputStream(compressed))
      val bzipInput = use(new BZip2CompressorInputStream(fileInput))

      val fileOutput = use(Files.newOutputStream(decompressed))
      val bufferedOutput = use(new BufferedOutputStream(fileOutput))

      bufferedOutput.write(bzipInput.readAllBytes())
    }
  }
}
