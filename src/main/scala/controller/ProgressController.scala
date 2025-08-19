package controller

import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, MenuBar, MenuItem}
import javafx.scene.chart.{LineChart, AreaChart, PieChart}
import javafx.scene.chart.XYChart
import javafx.collections.FXCollections
import scala.io.Source
import java.time.LocalDate
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.layout.HBox

class ProgressController {
  @FXML private var menuBar: MenuBar = _
  @FXML private var weightLineChart: LineChart[String, Number] = _
  @FXML private var caloriesTrendChart: AreaChart[String, Number] = _
  @FXML private var macroBreakdownChart: PieChart = _

  private val mealLogPath = "src/main/resources/meal_log.csv"
  private val activityLogPath = "src/main/resources/activity_log.csv"

  @FXML def initialize(): Unit = {
    // Wire top MenuBar navigation
    if (menuBar != null) {
      menuBar.getMenus.forEach { menu =>
        menu.getItems.forEach { item =>
          item.setOnAction(_ => Nav.switchFrom(menuBar, item.getText))
        }
      }
    }
    updateWeightChart()
    updateCaloriesChart()
    updateMacroChart()
    updateAchievements()
  }

  def updateWeightChart(): Unit = {
    // If you want to track weight, add a weight_log.csv and read it here
    // For now, use mock data
    val weightSeries = new XYChart.Series[String, Number]()
    weightSeries.setName("Weight")
    weightSeries.getData.addAll(
      new XYChart.Data("Mon", 70.5),
      new XYChart.Data("Tue", 70.3),
      new XYChart.Data("Wed", 70.2),
      new XYChart.Data("Thu", 70.0),
      new XYChart.Data("Fri", 69.8),
      new XYChart.Data("Sat", 69.7),
      new XYChart.Data("Sun", 69.6)
    )
    weightLineChart.getData.setAll(weightSeries)
  }

  def updateCaloriesChart(): Unit = {
    val intakeMap = scala.collection.mutable.Map[String, Int]()
    if (new java.io.File(mealLogPath).exists()) {
      val source = Source.fromFile(mealLogPath)
      source.getLines().drop(1).foreach { line =>
        val cols = line.split(",").map(_.trim)
        if (cols.length >= 3) {
          val date = cols(0)
          val cal = cols(2)
          intakeMap(date) = intakeMap.getOrElse(date, 0) + cal.toInt
        }
      }
      source.close()
    }
    val burnMap = scala.collection.mutable.Map[String, Int]()
    if (new java.io.File(activityLogPath).exists()) {
      val source = Source.fromFile(activityLogPath)
      source.getLines().drop(1).foreach { line =>
        val cols = line.split(",").map(_.trim)
        if (cols.length >= 4) {
          val date = cols(0)
          val cal = cols(3)
          burnMap(date) = burnMap.getOrElse(date, 0) + cal.toInt
        }
      }
      source.close()
    }
    val intakeSeries = new XYChart.Series[String, Number]()
    intakeSeries.setName("Intake")
    intakeMap.toSeq.sortBy(_._1).foreach { case (date, cal) =>
      intakeSeries.getData.add(new XYChart.Data(date, cal))
    }
    val burnSeries = new XYChart.Series[String, Number]()
    burnSeries.setName("Burn")
    burnMap.toSeq.sortBy(_._1).foreach { case (date, cal) =>
      burnSeries.getData.add(new XYChart.Data(date, cal))
    }
    caloriesTrendChart.getData.setAll(intakeSeries, burnSeries)
  }

  def updateMacroChart(): Unit = {
    val macros = Array(0.0, 0.0, 0.0)
    if (new java.io.File(mealLogPath).exists()) {
      val source = Source.fromFile(mealLogPath)
      source.getLines().drop(1).foreach { line =>
        val cols = line.split(",").map(_.trim)
        if (cols.length >= 6) {
          macros(0) += cols(3).toDouble
          macros(1) += cols(5).toDouble
          macros(2) += cols(4).toDouble
        }
      }
      source.close()
    }
    val pieData = FXCollections.observableArrayList[
      javafx.scene.chart.PieChart.Data](
      new javafx.scene.chart.PieChart.Data("Protein", macros(0)),
      new javafx.scene.chart.PieChart.Data("Carbs", macros(1)),
      new javafx.scene.chart.PieChart.Data("Fat", macros(2))
    )
    macroBreakdownChart.setData(pieData)
  }

  def updateAchievements(): Unit = {
    val intakeDays = scala.collection.mutable.Set[String]()
    val burnDays = scala.collection.mutable.Set[String]()
    if (new java.io.File(mealLogPath).exists()) {
      val source = Source.fromFile(mealLogPath)
      source.getLines().drop(1).foreach { line =>
        val parts = line.split(",")
        if (parts.nonEmpty) intakeDays += parts(0).trim
      }
      source.close()
    }
    if (new java.io.File(activityLogPath).exists()) {
      val source = Source.fromFile(activityLogPath)
      source.getLines().drop(1).foreach { line =>
        val parts = line.split(",")
        if (parts.nonEmpty) burnDays += parts(0).trim
      }
      source.close()
    }
    val streak = intakeDays.intersect(burnDays).size
    // Show streak and achievement labels
  }
}
