import java.io.File
import java.nio.file.Path
import scala.io.Source
import scala.util.Using

case class TextData(data: Array[(Array[String], String)], header: Array[String])

object TextLoader:
  val defaultDelimiter: String = ","

case class TextLoader(
    path: Path,
    header: Boolean = true,
    delimiter: String = TextLoader.defaultDelimiter
):
  def load(firtsCol: Int, lastCol: Int = -1): TextData =
    val (names, data) =
      Using.resource(Source.fromFile(path.toFile)) { s =>
        val lines = s.getLines()
        if header && lines.nonEmpty then
          val arr = lines.toArray
          (arr.head, arr.tail)
        else ("", lines.toArray)
      }

    def slice(a: Array[String]) =
      a.slice(
        firtsCol,
        if lastCol < 0 then a.length + lastCol else lastCol + 1
      )

    val rows = data
      .map(_.split(delimiter))
      .map(arr => slice(arr) -> arr.last)

    TextData(
      rows,
      slice(names.split(delimiter))
    )
