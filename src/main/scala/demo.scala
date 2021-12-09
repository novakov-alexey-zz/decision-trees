import decisiontree.api._

@main def main =
  val header = Array("color", "diameter", "label")
  val trainingData: Rows = List(
    (Vector("Green", 3), "Apple"),
    (Vector("Yellow", 3), "Apple"),
    (Vector("Red", 1), "Grape"),
    (Vector("Red", 1), "Grape"),
    (Vector("Yellow", 3), "Lemon")
  )

  val classifier = buildTree(trainingData)
  printTree(header, classifier, "")

  def printLeaf(counts: Map[String, Int]) =
    val total = counts.values.sum.toFloat
    counts.keys
      .map(label => label -> s"${(counts(label) / total * 100).toInt}%")
      .toMap

  val testingData: Rows = List(
    (Vector("Green", 3), "Apple"),
    (Vector("Yellow", 4), "Apple"),
    (Vector("Red", 2), "Grape"),
    (Vector("Red", 1), "Grape"),
    (Vector("Yellow", 3), "Lemon")
  )

  for (input, label) <- testingData do
    println(
      s"Actual: $label. Predicted: ${printLeaf(classifier.classify(input))}"
    )
