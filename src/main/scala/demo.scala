import decisiontree.api._
import java.nio.file.Paths
import decisiontree.DecisionTree
import java.nio.file.Path

@main def fruits =
  val header = Array("color", "diameter", "label")
  val trainingData = Array[Row](
    (Array("Green", 3), "Apple"),
    (Array("Yellow", 3), "Apple"),
    (Array("Red", 1), "Grape"),
    (Array("Red", 1), "Grape"),
    (Array("Yellow", 3), "Lemon")
  )

  val classifier = buildTree(trainingData)
  printer.toStdOut(header, classifier)

  def printLeaf(counts: Map[String, Int]) =
    val total = counts.values.sum.toFloat
    counts.keys
      .map(label => label -> s"${(counts(label) / total * 100).toInt}%")
      .toMap

  val testingData = Array[Row](
    (Array("Green", 3), "Apple"),
    (Array("Yellow", 4), "Apple"),
    (Array("Red", 2), "Grape"),
    (Array("Red", 1), "Grape"),
    (Array("Yellow", 3), "Lemon")
  )

  for (input, label) <- testingData do
    println(
      s"Actual: $label. Predicted: ${printLeaf(classifier.classify(input))}"
    )

  println(s"accuracy: ${accuracy(classifier, testingData)} %")

def accuracy(
    classifier: DecisionTree,
    data: Array[Row],
    exactMatch: Boolean = false
) =
  data.map { (input, label) =>
    val predictions = classifier.classify(input)
    if predictions.contains(label) then
      if predictions.size == 1 then 1f
      else if exactMatch then 0
      else 1f / predictions.size
    else 0
  }.sum * 100 / data.size

def splitArray[T](
    fraction: Float,
    data: Array[T]
): (Array[T], Array[T]) =
  val count = data.length * fraction
  val countOrZero = if count < 1 then 0 else count
  data.splitAt(data.length - countOrZero.toInt)

@main def customerChurn =
  val textData =
    DataLoader(Paths.get("data", "Churn_Modelling.csv")).load(3, -1)
  val rows =
    textData.data.map((features, label) =>
      Array[Data](
        features(0).toInt,
        features(1),
        features(2),
        features(3).toInt,
        features(4).toInt,
        features(5).toFloat,
        features(6).toInt,
        features(7).toInt,
        features(8).toInt,
        features(9).toFloat
      ) -> label
    )
  val (trainData, testData) = splitArray(0.2, rows)
  val classifier = buildTree(trainData)

  val names = printer.Names(
    textData.featureNames,
    Map("0" -> "No", "1" -> "Yes"),
    textData.variable
  )
  printer.toJsonFile(
    Path.of("output", "churn-modeling-tree.json"),
    names,
    classifier
  )
  println(s"train accuracy: ${accuracy(classifier, trainData)} %")
  println(s"test accuracy: ${accuracy(classifier, testData)} %")
