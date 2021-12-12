package decisiontree

import DecisionTree._
import Types._
import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import concurrent.ExecutionContext.Implicits.global

// CART algorithm (classification and regression trees)

object api:
  final type Row = decisiontree.Types.Row
  final type Rows = decisiontree.Types.Rows
  final type Data = decisiontree.Types.Data
  export decisiontree.buildTree
  export decisiontree.printTree
  export decisiontree.classify

def uniqueVals(data: Rows, col: Int) =
  data.map(_(col)).toSet

def classCounts(rows: Rows): Map[String, Int] =
  rows.groupMapReduce(_._2)(_ => 1)(_ + _)

def isNumeric(value: Data) =
  value match
    case _: String => false
    case _         => true

def partition(data: Rows, q: Question) =
  data.partition((features, _) => matches(q, features))

def gini(data: Rows): Float =
  val counts = classCounts(data)
  counts.keys.foldLeft(1f) { (impurity, label) =>
    val labelProb = counts(label) / data.size.toFloat
    impurity - math.pow(labelProb, 2).toFloat
  }

def infoGain(left: Rows, right: Rows, currentUncertainty: Float) =
  val p = left.length.toFloat / (left.length + right.length)
  currentUncertainty - p * gini(left) - (1 - p) * gini(right)

def findBestSplit(rows: Rows): (Float, Option[Question]) =
  var bestGain = 0f
  var bestQuestion = Option.empty[Question]
  val currentUncertainty = gini(rows)
  val featureCount = rows.headOption.map(_._1.length - 1).getOrElse(0)

  for col <- 0 until featureCount do
    val uniqueVals = rows.map((f, _) => f(col)).toSet

    for value <- uniqueVals do
      val question = Question(col, value)
      val (trueRows, falseRows) = partition(rows, question)
      // Skip this split if it doesn't divide the dataset.
      if trueRows.nonEmpty && falseRows.nonEmpty then
        // Calculate the information gain from this split
        val gain = infoGain(trueRows, falseRows, currentUncertainty)
        // You actually can use '>' instead of '>=' here
        // but I wanted the tree to look a certain way for our toy dataset.
        if gain >= bestGain then
          bestGain = gain
          bestQuestion = Some(question)

  (bestGain, bestQuestion)

def leaf(data: Rows): Leaf =
  Leaf(classCounts(data))

def matches(q: Question, example: Features): Boolean =
  val exampleVal = example(q.col)
  if isNumeric(exampleVal) then
    (exampleVal, q.value) match
      case (i: Int, i2: Int)     => i >= i2
      case (f: Float, f2: Float) => f >= f2
      case _                     => false
  else exampleVal == q.value

def buildTree(rows: Rows): DecisionTree =
  Await.result(buildTreeAsync(rows), 30.seconds)

private def buildTreeAsync(rows: Rows): Future[DecisionTree] =
  val (gain, question) = findBestSplit(rows)
  question match
    case None => Future(leaf(rows))
    case Some(q) =>
      if gain == 0 then Future(leaf(rows))
      else
        // # If we reach here, we have found a useful feature / value to partition on.
        println(s"buidling node for: $q")
        val (trueRows, falseRows) = partition(rows, q)
        val trueBranch = buildTreeAsync(trueRows)
        val falseBranch = buildTreeAsync(falseRows)
        val branches = Future.sequence(List(trueBranch, falseBranch))
        branches.map(list => DecisionNode(q, list.head, list.last))

extension (node: DecisionTree)
  def classify(input: Features): Map[String, Int] =
    @tailrec
    def loop(
        input: Features,
        node: DecisionTree
    ): Map[String, Int] =
      node match
        case Leaf(predictions) => predictions
        case DecisionNode(question, trueBranch, falseBranch) =>
          if matches(question, input) then loop(input, trueBranch)
          else loop(input, falseBranch)

    loop(input, node)

def asString(q: Question, header: Array[String]) =
  val condition = if isNumeric(q.value) then ">=" else "=="
  s"Is ${header(q.col)} $condition ${q.value}"

def printTree(
    header: Array[String],
    node: DecisionTree,
    spacing: String = ""
): Unit =
  node match
    case Leaf(predictions) =>
      println(spacing + "Predict: " + predictions)
    case DecisionNode(question, trueBranch, falseBranch) =>
      println(spacing + asString(question, header))

      println(spacing + "--> True:")
      printTree(header, trueBranch, spacing + "  ")

      println(spacing + "--> False:")
      printTree(header, falseBranch, spacing + "  ")
