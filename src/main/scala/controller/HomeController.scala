package controller

import javafx.fxml.FXML
import javafx.scene.control.{Button, ProgressBar, Label}
import javafx.scene.chart.PieChart
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import java.io.{BufferedWriter, FileWriter}
import java.time.LocalDate
import javafx.scene.control.TextInputDialog
import java.util.Optional
import scala.io.Source
import java.util.Properties
import java.io.{File, FileInputStream, FileOutputStream}

class HomeController {
  @FXML private var homeBtn: Button = _
  @FXML private var foodLoggingBtn: Button = _
  @FXML private var activityBtn: Button = _
  @FXML private var goalsBtn: Button = _
  @FXML private var progressBtn: Button = _
  @FXML private var notificationsBtn: Button = _
  @FXML private var profileBtn: Button = _
  @FXML private var quickAddFoodBtn: Button = _
  @FXML private var quickAddActivityBtn: Button = _
  @FXML private var calorieProgress: ProgressBar = _
  @FXML private var macroPieChart: PieChart = _
  @FXML private var stepsLabel: Label = _
  @FXML private var exerciseLabel: Label = _
  @FXML private var hydrationLabel: Label = _
  @FXML private var quoteLabel: Label = _
  @FXML private var inputStepsBtn: Button = _
  @FXML private var inputExerciseBtn: Button = _
  @FXML private var inputHydrationBtn: Button = _

  private val mealLogPath = "src/main/resources/meal_log.csv"
  private val activityLogPath = "src/main/resources/activity_log.csv"
  private val userStatsPath = "src/main/resources/user_stats.conf"
  private val goalsConfPath = "src/main/resources/goals.conf"
  private val statsProps = new Properties()

  @FXML def initialize(): Unit = {
    // Load user stats
    if (new java.io.File(userStatsPath).exists()) {
      val fis = new FileInputStream(userStatsPath)
      statsProps.load(fis)
      fis.close()
      stepsLabel.setText(s"Steps: ${statsProps.getProperty("steps", "8000")}")
      exerciseLabel.setText(s"Exercise: ${statsProps.getProperty("exercise", "30 min")}")
      hydrationLabel.setText(s"Hydration: ${statsProps.getProperty("hydration", "2L")}")
    }
    updateMacrosAndProgress()
    quickAddFoodBtn.setOnAction((_: ActionEvent) => quickAddFood())
    quickAddActivityBtn.setOnAction((_: ActionEvent) => quickAddActivity())
    inputStepsBtn.setOnAction((_: ActionEvent) => inputSteps())
    inputExerciseBtn.setOnAction((_: ActionEvent) => inputExercise())
    inputHydrationBtn.setOnAction((_: ActionEvent) => inputHydration())
  }

  def quickAddFood(): Unit = {
    val dialog = new TextInputDialog()
    dialog.setTitle("Quick Add Food")
    dialog.setHeaderText("Enter calories, protein, fat, carbs (comma separated)")
    dialog.setContentText("e.g. 250,10,5,40")
    val result: Optional[String] = dialog.showAndWait()
    if (result.isPresent) {
      val input = result.get.split(",").map(_.trim)
      if (input.length == 4 && input.forall(s => s.matches("[0-9.]+"))) {
        val today = LocalDate.now()
        val bw = new BufferedWriter(new FileWriter(mealLogPath, true))
        bw.write(s"$today,QuickAdd,${input(0)},${input(1)},${input(2)},${input(3)}\n")
        bw.close()
        updateMacrosAndProgress()
        val alert = new Alert(AlertType.INFORMATION)
        alert.setTitle("Food Added")
        alert.setHeaderText(null)
        alert.setContentText(s"Quick added: ${input.mkString(", ")}")
        alert.showAndWait()
      }
    }
  }

  def quickAddActivity(): Unit = {
    val dialog = new TextInputDialog()
    dialog.setTitle("Quick Add Activity")
    dialog.setHeaderText("Enter duration (min), calories burned")
    dialog.setContentText("e.g. 30,200")
    val result: Optional[String] = dialog.showAndWait()
    if (result.isPresent) {
      val input = result.get.split(",").map(_.trim)
      if (input.length == 2 && input.forall(s => s.matches("[0-9.]+"))) {
        val today = LocalDate.now()
        val bw = new BufferedWriter(new FileWriter(activityLogPath, true))
        bw.write(s"$today,QuickAdd,${input(0)},${input(1)}\n")
        bw.close()
        val alert = new Alert(AlertType.INFORMATION)
        alert.setTitle("Activity Added")
        alert.setHeaderText(null)
        alert.setContentText(s"Quick added: ${input.mkString(", ")}")
        alert.showAndWait()
      }
    }
  }

  def updateMacrosAndProgress(): Unit = {
    val calTarget = {
      val p = FileUtil.loadProperties(goalsConfPath)
      Option(p.getProperty("calorieTarget")).flatMap(s=>scala.util.Try(s.toDouble).toOption).getOrElse(2200.0)
    }
    // Sum today's macros and calories
    val today = LocalDate.now().toString
    var totalCal = 0.0
    var totalPro = 0.0
    var totalFat = 0.0
    var totalCarb = 0.0
    if (new java.io.File(mealLogPath).exists()) {
      val source = Source.fromFile(mealLogPath)
      source.getLines().drop(1).foreach { line =>
        val parts = line.split(",").map(_.trim)
        if (parts.length==6 && parts(0)==today) {
          scala.util.Try {
            totalCal += parts(2).toDouble
            totalPro += parts(3).toDouble
            totalFat += parts(4).toDouble
            totalCarb += parts(5).toDouble
          }
        }
      }
      source.close()
    }
    calorieProgress.setProgress(math.min(1.0, totalCal / calTarget))
    val pieData = FXCollections.observableArrayList[
      PieChart.Data](
      new PieChart.Data("Protein", totalPro),
      new PieChart.Data("Carbs", totalCarb),
      new PieChart.Data("Fat", totalFat)
    )
    macroPieChart.setData(pieData)
    quoteLabel.setText(fuelQuote(totalCal, calTarget))
  }

  private def fuelQuote(cal: Double, target: Double): String = {
    val pct = cal/target
    if (pct < 0.33) "Strong start!" else if (pct < 0.66) "Keep fueling wisely." else if (pct < 0.95) "Almost there!" else if (pct <= 1.05) "Goal met nicely!" else "Slightly over—balance tomorrow."  }

  def inputSteps(): Unit = {
    val dialog = new TextInputDialog()
    dialog.setTitle("Input Steps")
    dialog.setHeaderText("Enter steps for today")
    dialog.setContentText("e.g. 9000")
    val result: Optional[String] = dialog.showAndWait()
    if (result.isPresent && result.get.matches("[0-9]+")) {
      statsProps.setProperty("steps", result.get)
      val fos = new FileOutputStream(userStatsPath)
      statsProps.store(fos, null)
      fos.close()
      stepsLabel.setText(s"Steps: ${result.get}")
    }
  }

  def inputExercise(): Unit = {
    val dialog = new TextInputDialog()
    dialog.setTitle("Input Exercise")
    dialog.setHeaderText("Enter exercise duration (min)")
    dialog.setContentText("e.g. 45")
    val result: Optional[String] = dialog.showAndWait()
    if (result.isPresent && result.get.matches("[0-9]+")) {
      statsProps.setProperty("exercise", s"${result.get} min")
      val fos = new FileOutputStream(userStatsPath)
      statsProps.store(fos, null)
      fos.close()
      exerciseLabel.setText(s"Exercise: ${result.get} min")
    }
  }

  def inputHydration(): Unit = {
    val dialog = new TextInputDialog()
    dialog.setTitle("Input Hydration")
    dialog.setHeaderText("Enter hydration (L)")
    dialog.setContentText("e.g. 2.5")
    val result: Optional[String] = dialog.showAndWait()
    if (result.isPresent && result.get.matches("[0-9.]+")) {
      statsProps.setProperty("hydration", s"${result.get}L")
      val fos = new FileOutputStream(userStatsPath)
      statsProps.store(fos, null)
      fos.close()
      hydrationLabel.setText(s"Hydration: ${result.get}L")
    }
  }
}
