package controller

abstract class FoodItem(val name: String):
  def calories: Int
  def protein: Double
  def fat: Double
  def carbs: Double

class SimpleFood(
                  name: String,
                  val cal: Int,
                  val pro: Double,
                  val fatVal: Double,
                  val carbVal: Double
                ) extends FoodItem(name):
  def calories: Int = cal
  def protein: Double = pro
  def fat: Double = fatVal
  def carbs: Double = carbVal
