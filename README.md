# Decision Tree Algorithm

Based on [decision_tree.py](https://github.com/random-forests/tutorials/blob/master/decision_tree.py)

# Demos

Run "customerChurn" example:

```bash
sbt run
> 1
```

Run "fruits" example:

```bash
sbt run
> 2
```

# Usage

```scala
import decisiontree.api._
import decisiontree.DecisionTree

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
```

```bash
Is color == Red
--> True:
  Predict: Map(Grape -> 2)
--> False:
  Is color == Green
  --> True:
    Predict: Map(Apple -> 1)
  --> False:
    Predict: Map(Apple -> 1, Lemon -> 1)
Actual: Apple. Predicted: Map(Apple -> 100%)
Actual: Apple. Predicted: Map(Apple -> 50%, Lemon -> 50%)
Actual: Grape. Predicted: Map(Grape -> 100%)
Actual: Grape. Predicted: Map(Grape -> 100%)
Actual: Lemon. Predicted: Map(Apple -> 50%, Lemon -> 50%)
```
