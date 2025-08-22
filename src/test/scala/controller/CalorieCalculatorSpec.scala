package controller

import org.scalatest.funsuite.AnyFunSuite

class CalorieCalculatorSpec extends AnyFunSuite {
  test("computeTarget returns higher value for more active profile") {
    val sedentary = CalorieCalculator.computeTarget(30, "Male", 180, 80, "Sedentary", "Maintain Weight")
    val active = CalorieCalculator.computeTarget(30, "Male", 180, 80, "Active", "Maintain Weight")
    assert(active > sedentary)
  }
  test("computeTarget applies weight loss adjustment (lower than maintain)") {
    val maintain = CalorieCalculator.computeTarget(28, "Female", 165, 60, "Lightly Active", "Maintain Weight")
    val lose = CalorieCalculator.computeTarget(28, "Female", 165, 60, "Lightly Active", "Lose Weight")
    assert(lose < maintain)
  }
  test("computeTarget enforces minimum of 1200") {
    val target = CalorieCalculator.computeTarget(75, "Female", 150, 45, "Sedentary", "Lose Weight")
    assert(target >= 1200)
  }
}
