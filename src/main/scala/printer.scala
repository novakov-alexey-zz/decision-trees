import decisiontree._
import decisiontree.DecisionTree._
import java.io.PrintWriter
import scala.util.Using
import java.nio.file.Path
import printer._

object printer:

  def toStdOut(
      featureNames: Array[String],
      tree: DecisionTree,
      spacing: String = ""
  ): Unit =
    tree match
      case Leaf(predictions) =>
        println(spacing + "Predict: " + predictions)
      case Node(question, trueBranch, falseBranch) =>
        println(spacing + asString(question, featureNames))

        println(spacing + "--> True:")
        toStdOut(featureNames, trueBranch, spacing + "  ")

        println(spacing + "--> False:")
        toStdOut(featureNames, falseBranch, spacing + "  ")

  private def toJson(
      names: Names,
      tree: DecisionTree,
      writer: PrintWriter,
      spacing: String = ""
  ): Unit =
    tree match
      case Leaf(predictions) =>
        val predictionJson = predictions
          .map((label, count) =>
            s""" "${names.variable}" : "${names.labels(
              label
            )}", "count" : $count """
          )
          .mkString(",")
        writer.write(spacing + s"""{"prediction" : { $predictionJson } }""")
        writer.println
      case Node(question, trueBranch, falseBranch) =>
        writer.write(
          spacing + s"""{"question": "${asString(
            question,
            names.features
          )}", """
        )
        writer.println
        writer.write(spacing + s""""branches" : {""")
        writer.println

        writer.write(spacing + """"true": """)
        writer.println
        toJson(names, trueBranch, writer, spacing + "  ")
        writer.write(spacing + "")

        writer.println
        writer.write(spacing + """, "false": """)
        writer.println
        toJson(names, falseBranch, writer, spacing + "  ")
        writer.write(spacing + "")

        writer.write(spacing + "}")
        writer.println
        writer.write(spacing + "}")
        writer.println

  case class Names(
      features: Array[String],
      labels: Map[String, String],
      variable: String
  )

  def toJsonFile(
      path: Path,
      names: Names,
      tree: DecisionTree
  ): Unit =
    Using.resource(PrintWriter(path.toFile)) { w =>
      toJson(names, tree, w)
    }
