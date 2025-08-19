package controller

import javafx.fxml.FXML
import javafx.scene.control.{Button, CheckBox, Spinner}
import javafx.event.ActionEvent
import java.util.Properties
import java.io.{FileInputStream, FileOutputStream}

class NotificationsController {
  @FXML private var breakfastReminder: CheckBox = _
  @FXML private var lunchReminder: CheckBox = _
  @FXML private var dinnerReminder: CheckBox = _
  @FXML private var snacksReminder: CheckBox = _
  @FXML private var goalAlert: CheckBox = _
  @FXML private var reminderTimeSpinner: Spinner[Integer] = _
  @FXML private var saveNotificationsBtn: Button = _

  private val notificationsConfPath = "src/main/resources/notifications.conf"
  private val props = new Properties()

  @FXML def initialize(): Unit = {
    // Load notifications from config
    if (new java.io.File(notificationsConfPath).exists()) {
      val fis = new FileInputStream(notificationsConfPath)
      props.load(fis)
      fis.close()
      breakfastReminder.setSelected(props.getProperty("breakfast", "true").toBoolean)
      lunchReminder.setSelected(props.getProperty("lunch", "true").toBoolean)
      dinnerReminder.setSelected(props.getProperty("dinner", "false").toBoolean)
      snacksReminder.setSelected(props.getProperty("snacks", "false").toBoolean)
      goalAlert.setSelected(props.getProperty("goalAlert", "true").toBoolean)
      reminderTimeSpinner.getValueFactory.setValue(props.getProperty("reminderTime", "8").toInt)
    }
    saveNotificationsBtn.setOnAction((_: ActionEvent) => saveNotifications())
  }

  def saveNotifications(): Unit = {
    props.setProperty("breakfast", breakfastReminder.isSelected.toString)
    props.setProperty("lunch", lunchReminder.isSelected.toString)
    props.setProperty("dinner", dinnerReminder.isSelected.toString)
    props.setProperty("snacks", snacksReminder.isSelected.toString)
    props.setProperty("goalAlert", goalAlert.isSelected.toString)
    props.setProperty("reminderTime", reminderTimeSpinner.getValue.toString)
    val fos = new FileOutputStream(notificationsConfPath)
    props.store(fos, null)
    fos.close()
  }
}
