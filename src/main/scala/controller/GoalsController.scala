package controller

import java.util.Properties
import java.io.{FileInputStream, FileOutputStream}
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.{Button, ChoiceBox, TextField, Slider, Label}
import javafx.scene.layout.HBox
import javafx.collections.FXCollections

class GoalsController {
  @FXML private var homeBtn: Button = _
  @FXML private var foodLoggingBtn: Button = _
  @FXML private var activityBtn: Button = _
  @FXML private var goalsBtn: Button = _
  @FXML private var progressBtn: Button = _
  @FXML private var notificationsBtn: Button = _
  @FXML private var profileBtn: Button = _
  @FXML private var weightGoalChoice: ChoiceBox[String] = _
  @FXML private var calorieTargetField: TextField = _
  @FXML private var carbsSlider: Slider = _
  @FXML private var proteinSlider: Slider = _
  @FXML private var fatSlider: Slider = _
  @FXML private var waterGoalField: TextField = _
  @FXML private var saveGoalsBtn: Button = _

  private val goalsConfPath = "src/main/resources/goals.conf"
  private val props = new Properties()

  @FXML def initialize(): Unit = {
    // Load goals from config
    val fis = new FileInputStream(goalsConfPath)
    props.load(fis)
    fis.close()
    weightGoalChoice.setItems(FXCollections.observableArrayList("Lose Weight", "Maintain Weight", "Gain Weight"))
    weightGoalChoice.setValue(props.getProperty("weightGoal", "Maintain Weight"))
    calorieTargetField.setText(props.getProperty("calorieTarget", "2200"))
    carbsSlider.setValue(props.getProperty("carbs", "50").toDouble)
    proteinSlider.setValue(props.getProperty("protein", "30").toDouble)
    fatSlider.setValue(props.getProperty("fat", "20").toDouble)
    waterGoalField.setText(props.getProperty("waterGoal", "2.0"))
    saveGoalsBtn.setOnAction((_: ActionEvent) => saveGoals())
  }

  def saveGoals(): Unit = {
    props.setProperty("weightGoal", weightGoalChoice.getValue)
    props.setProperty("calorieTarget", calorieTargetField.getText)
    props.setProperty("carbs", carbsSlider.getValue.toInt.toString)
    props.setProperty("protein", proteinSlider.getValue.toInt.toString)
    props.setProperty("fat", fatSlider.getValue.toInt.toString)
    props.setProperty("waterGoal", waterGoalField.getText)
    val fos = new FileOutputStream(goalsConfPath)
    props.store(fos, null)
    fos.close()
  }
}
