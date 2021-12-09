package decisiontree

import Types._

object Types:
  type Label = String
  type Data = Int | Float | String
  type Features = Vector[Data]
  type Rows = List[(Features, Label)]

case class Question(col: Int, value: Data)

enum DecisionTree:
  case Leaf(predictions: Map[Label, Int])

  case DecisionNode(
      q: Question,
      trueBranch: DecisionTree,
      falseBranch: DecisionTree
  )
