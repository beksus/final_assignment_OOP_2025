package controller

import javafx.fxml.FXML
import javafx.scene.control.{Label, MenuBar}
import javafx.scene.chart.{LineChart, AreaChart, PieChart, XYChart}
import javafx.collections.FXCollections
import scala.io.Source
import java.time.LocalDate
import java.time.format.DateTimeParseException

class ProgressController {
  @FXML private var menuBar: MenuBar = _
  @FXML private var weightLineChart: LineChart[String, Number] = _
  @FXML private var caloriesTrendChart: AreaChart[String, Number] = _
  @FXML private var macroBreakdownChart: PieChart = _
  @FXML private var streakLabel: Label = _
  @FXML private var daysLoggedLabel: Label = _
  @FXML private var intakeGoalLabel: Label = _

  private val mealLogPath = "src/main/resources/meal_log.csv"
  private val activityLogPath = "src/main/resources/activity_log.csv"
  private val weightLogPath = "src/main/resources/weight_log.csv"
  private val goalsConfPath = "src/main/resources/goals.conf"

  @FXML def initialize(): Unit = {
    println("[ProgressController] initialize start")
    safe("wireMenu") { if (menuBar != null) Nav.wireNav(menuBar.getParent.asInstanceOf[javafx.scene.Parent]) }
    safe("weightChart")(updateWeightChart())
    safe("caloriesChart")(updateCaloriesChart())
    safe("macroChart")(updateMacroChart())
    safe("achievements")(updateAchievements())
    println("[ProgressController] initialize done")
  }

  private def safe(tag:String)(body: => Unit): Unit = {
    try body catch { case ex: Throwable =>
      System.err.println(s"[ProgressController] $tag failed: ${ex.getMessage}")
      ex.printStackTrace()
    }
  }

  def updateWeightChart(): Unit = {
    if (weightLineChart == null) { println("[ProgressController] weightLineChart null"); return }
    val series = new XYChart.Series[String, Number]()
    series.setName("Weight (kg)")
    val entries = FileUtil.readWeightLog(weightLogPath)
    if (entries.nonEmpty) {
      val sorted = entries.flatMap { case (d, w) =>
        try Some(LocalDate.parse(d) -> w) catch { case _: DateTimeParseException => None }
      }.sortBy(_._1).takeRight(120)
      sorted.foreach { case (date, weight) => series.getData.add(new XYChart.Data(date.toString, weight)) }
    } else {
      series.getData.add(new XYChart.Data(LocalDate.now().toString, 0))
    }
    weightLineChart.getData.setAll(series)
  }

  def updateCaloriesChart(): Unit = {
    if (caloriesTrendChart == null) { println("[ProgressController] caloriesTrendChart null"); return }
    val intakeMap = scala.collection.mutable.Map[String, Int]()
    if (new java.io.File(mealLogPath).exists()) {
      safe("readMeals") {
        val source = Source.fromFile(mealLogPath)
        try source.getLines().drop(1).foreach { line =>
          val cols = line.split(",").map(_.trim)
            if (cols.length >= 3 && cols(2).matches("[0-9]+")) {
              val date = cols(0)
              intakeMap(date) = intakeMap.getOrElse(date, 0) + cols(2).toInt
            }
        } finally source.close()
      }
    }
    val burnMap = scala.collection.mutable.Map[String, Int]()
    if (new java.io.File(activityLogPath).exists()) {
      safe("readActivity") {
        val source = Source.fromFile(activityLogPath)
        try source.getLines().drop(1).foreach { line =>
          val cols = line.split(",").map(_.trim)
          if (cols.length >= 4 && cols(3).matches("[0-9]+")) {
            val date = cols(0)
            burnMap(date) = burnMap.getOrElse(date, 0) + cols(3).toInt
          }
        } finally source.close()
      }
    }
    val intakeSeries = new XYChart.Series[String, Number](); intakeSeries.setName("Intake")
    intakeMap.toSeq.sortBy(_._1).foreach { case (d, c) => intakeSeries.getData.add(new XYChart.Data(d, c)) }
    if (intakeSeries.getData.isEmpty) intakeSeries.getData.add(new XYChart.Data(LocalDate.now().toString, 0))
    val burnSeries = new XYChart.Series[String, Number](); burnSeries.setName("Burn")
    burnMap.toSeq.sortBy(_._1).foreach { case (d, c) => burnSeries.getData.add(new XYChart.Data(d, c)) }
    if (burnSeries.getData.isEmpty) burnSeries.getData.add(new XYChart.Data(LocalDate.now().toString, 0))
    caloriesTrendChart.getData.setAll(intakeSeries, burnSeries)
  }

  def updateMacroChart(): Unit = {
    if (macroBreakdownChart == null) { println("[ProgressController] macroBreakdownChart null"); return }
    val macros = Array(0.0, 0.0, 0.0) // protein, carbs, fat
    if (new java.io.File(mealLogPath).exists()) {
      safe("readMacros") {
        val source = Source.fromFile(mealLogPath)
        try source.getLines().drop(1).foreach { line =>
          val cols = line.split(",").map(_.trim)
          if (cols.length >= 6 && cols.slice(3,6).forall(_.matches("[0-9.]+"))) {
            macros(0) += cols(3).toDouble
            macros(1) += cols(5).toDouble
            macros(2) += cols(4).toDouble
          }
        } finally source.close()
      }
    }
    val total = macros.sum match { case 0 => 1.0; case s => s }
    val pieData = FXCollections.observableArrayList[
      javafx.scene.chart.PieChart.Data](
      new javafx.scene.chart.PieChart.Data("Protein", macros(0) match {case 0=>1; case v=>v}),
      new javafx.scene.chart.PieChart.Data("Carbs", macros(1) match {case 0=>1; case v=>v}),
      new javafx.scene.chart.PieChart.Data("Fat", macros(2) match {case 0=>1; case v=>v})
    )
    macroBreakdownChart.setData(pieData)
  }

  private def loadCalorieTarget(): Int = {
    val p = FileUtil.loadProperties(goalsConfPath)
    Option(p.getProperty("calorieTarget")).flatMap(s=>scala.util.Try(s.toInt).toOption).getOrElse(2200)
  }

  private def updateAchievements(): Unit = {
    if (streakLabel==null && daysLoggedLabel==null && intakeGoalLabel==null) return
    val intakeMap = scala.collection.mutable.Map[String,Int]()
    val burnDays = scala.collection.mutable.Set[String]()
    if (new java.io.File(mealLogPath).exists()) {
      safe("readMealForAch") {
        val src = Source.fromFile(mealLogPath)
        try src.getLines().drop(1).foreach { line =>
          val parts = line.split(",").map(_.trim)
          if (parts.length>=3 && parts(2).matches("[0-9]+")) {
            intakeMap(parts(0)) = intakeMap.getOrElse(parts(0),0) + parts(2).toInt
          }
        } finally src.close()
      }
    }
    if (new java.io.File(activityLogPath).exists()) {
      safe("readActivityForAch") {
        val src = Source.fromFile(activityLogPath)
        try src.getLines().drop(1).foreach { line =>
          val parts = line.split(",").map(_.trim)
          if (parts.nonEmpty) burnDays += parts(0)
        } finally src.close()
      }
    }
    val stats = AchievementUtil.computeStats(intakeMap.toMap, burnDays.toSet, loadCalorieTarget())
    if (streakLabel!=null) streakLabel.setText(s"Streak: ${stats.streak}")
    if (daysLoggedLabel!=null) daysLoggedLabel.setText(s"Logged Days: ${stats.loggedDays}")
    if (intakeGoalLabel!=null) intakeGoalLabel.setText(s"Intake Goal Days: ${stats.intakeGoalDays}")
  }
}
