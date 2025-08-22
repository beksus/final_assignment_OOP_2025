package controller

object CalorieCalculator {
  /**
    * Compute daily calorie target using Mifflin-St Jeor + activity + goal adjustment.
    * @param age years
    * @param gender Male/Female/Other
    * @param heightCm centimetres
    * @param weightKg kilograms
    * @param activity Sedentary/Lightly Active/Active/Very Active
    * @param weightGoal Lose Weight / Maintain Weight / Gain Weight
    * @return calorie target (>=1200)
    */
  def computeTarget(age:Int, gender:String, heightCm:Int, weightKg:Double, activity:String, weightGoal:String): Int = {
    val base = gender match {
      case g if g.equalsIgnoreCase("Male") => 10*weightKg + 6.25*heightCm - 5*age + 5
      case g if g.equalsIgnoreCase("Female") => 10*weightKg + 6.25*heightCm - 5*age - 161
      case _ => 10*weightKg + 6.25*heightCm - 5*age
    }
    val factor = activity match {
      case a if a.startsWith("Sedentary") => 1.2
      case a if a.startsWith("Lightly") => 1.375
      case a if a.startsWith("Active") => 1.55
      case a if a.startsWith("Very") => 1.725
      case _ => 1.5
    }
    val adjust = weightGoal match {
      case g if g.startsWith("Lose") => -500
      case g if g.startsWith("Gain") => 300
      case _ => 0
    }
    math.max(1200, math.round(base * factor + adjust).toInt)
  }
}
