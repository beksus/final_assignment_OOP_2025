package controller

import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, MenuBar, TextField}
import javafx.scene.chart.BarChart
import javafx.scene.chart.XYChart
import java.io.{BufferedWriter, FileWriter}
import scala.io.Source
import java.time.LocalDate
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.event.ActionEvent
import scala.collection.mutable

class ActivityTrackingController {
  @FXML private var menuBar: MenuBar = _
  @FXML private var workoutNameField: TextField = _
  @FXML private var durationField: TextField = _
  @FXML private var addWorkoutBtn: Button = _
  @FXML private var syncDeviceBtn: Button = _
  @FXML private var stepsLabel: Label = _
  @FXML private var distanceLabel: Label = _
  @FXML private var heartRateLabel: Label = _
  @FXML private var activeMinutesLabel: Label = _
  @FXML private var caloriesBurnedChart: BarChart[String, Number] = _

  private val activityLogPath = "src/main/resources/activity_log.csv"

  @FXML def initialize(): Unit = {
    // Wire MenuBar navigation
    if (menuBar != null) {
      menuBar.getMenus.forEach { menu =>
        menu.getItems.forEach { item =>
          item.setOnAction(_ => Nav.switchFrom(menuBar, item.getText))
        }
      }
    }
    stepsLabel.setText("Steps: 8,000")
    distanceLabel.setText("Distance: 5 km")
    heartRateLabel.setText("Heart Rate: 75 bpm")
    activeMinutesLabel.setText("Active Minutes: 45")
    addWorkoutBtn.setOnAction((_: ActionEvent) => {
      val workout = workoutNameField.getText.trim
      val durationStr = workoutNameField.getScene // keep ref; not needed
      val dStr = durationField.getText.trim
      val ds = dStr
      if (workout.nonEmpty && ds.nonEmpty && ds.forall(_.isDigit)) {
        val duration = ds.toInt
        val calories = duration * 8 // Simple estimation
        val today = LocalDate.now()
        val bw = new BufferedWriter(new FileWriter(activityLogPath, true))
        if (new java.io.File(activityLogPath).length() == 0) {
          bw.write("date,workout,duration,calories\n")
        }
        bw.write(s"$today,$workout,$duration,$calories\n")
        bw.close()
        val alert = new Alert(AlertType.INFORMATION)
        alert.setTitle("Workout Logged")
        alert.setHeaderText(null)
        alert.setContentText(s"Logged $workout ($duration min, $calories kcal)")
        alert.showAndWait()
        updateChart()
      }
    })
    updateChart()
  }

  def updateChart(): Unit = {
    val file = new java.io.File(activityLogPath)
    if (!file.exists()) {
      caloriesBurnedChart.getData.clear()
      return
    }
    val source = Source.fromFile(file)
    val lines = source.getLines().drop(1).toList
    val dayCalories = mutable.Map[String, Int]()
    lines.foreach { line =>
      val cols = line.split(",").map(_.trim)
      if (cols.length >= 4) {
        val date = cols(0)
        val calories = cols(3)
        dayCalories(date) = dayCalories.getOrElse(date, 0) + calories.toInt
      }
    }
    source.close()
    val series = new XYChart.Series[String, Number]()
    series.setName("Calories Burned")
    dayCalories.toSeq.sortBy(_._1).foreach { case (date, cal) =>
      series.getData.add(new XYChart.Data(date, cal))
    }
    caloriesBurnedChart.getData.setAll(series)
  }
}
