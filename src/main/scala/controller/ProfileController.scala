package controller

import javafx.fxml.FXML
import javafx.scene.control.{TextField, ChoiceBox, Button, MenuBar, Alert}
import javafx.collections.FXCollections
import java.util.Properties
import java.io.{File, FileInputStream, FileOutputStream}
import scala.util.Try
import javafx.stage.FileChooser
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

class ProfileController {
  @FXML private var menuBar: MenuBar = _
  @FXML private var ageField: TextField = _
  @FXML private var genderChoice: ChoiceBox[String] = _
  @FXML private var heightField: TextField = _
  @FXML private var weightField: TextField = _
  @FXML private var activityLevelChoice: ChoiceBox[String] = _
  @FXML private var googleFitBtn: Button = _
  @FXML private var fitbitBtn: Button = _
  @FXML private var exportBtn: Button = _
  @FXML private var themeChoice: ChoiceBox[String] = _
  @FXML private var saveProfileBtn: Button = _
  @FXML private var exportZipBtn: Button = _
  @FXML private var importZipBtn: Button = _

  private val profileConfPath = "src/main/resources/profile.conf"
  private val goalsConfPath = "src/main/resources/goals.conf"

  private val profileProps = new Properties()
  private val goalsProps = new Properties()

  @FXML def initialize(): Unit = {
    if (menuBar != null) {
      menuBar.getMenus.forEach { menu => menu.getItems.forEach { item => item.setOnAction(_ => Nav.switchFrom(menuBar, item.getText)) } }
    }
    genderChoice.setItems(FXCollections.observableArrayList("Male", "Female", "Other"))
    activityLevelChoice.setItems(FXCollections.observableArrayList("Sedentary", "Lightly Active", "Active", "Very Active"))
    themeChoice.setItems(FXCollections.observableArrayList("Light", "Dark"))
    loadExisting()
    if (saveProfileBtn != null) saveProfileBtn.setOnAction(_ => saveProfile())
    if (exportZipBtn != null) exportZipBtn.setOnAction(_ => exportData())
    if (importZipBtn != null) importZipBtn.setOnAction(_ => importData())
    if (themeChoice != null) themeChoice.getSelectionModel.selectedItemProperty.addListener((_,_,_) => MainApp.refreshTheme())
  }

  private def loadExisting(): Unit = {
    val pf = new File(profileConfPath)
    if (pf.exists()) {
      val fis = new FileInputStream(pf); profileProps.load(fis); fis.close()
      ageField.setText(profileProps.getProperty("age", "28"))
      genderChoice.setValue(profileProps.getProperty("gender", "Male"))
      heightField.setText(profileProps.getProperty("height", "175"))
      weightField.setText(profileProps.getProperty("weight", "70"))
      activityLevelChoice.setValue(profileProps.getProperty("activity", "Active"))
      themeChoice.setValue(profileProps.getProperty("theme", "Light"))
    } else {
      ageField.setText("28"); genderChoice.setValue("Male"); heightField.setText("175"); weightField.setText("70"); activityLevelChoice.setValue("Active"); themeChoice.setValue("Light")
    }
    val gf = new File(goalsConfPath)
    if (gf.exists()) { val fis = new FileInputStream(gf); goalsProps.load(fis); fis.close() }
  }

  private def saveProfile(): Unit = {
    val ageOpt = toInt(ageField.getText)
    val heightOpt = toInt(heightField.getText)
    val weightOpt = toDouble(weightField.getText)
    if (ageOpt.isEmpty || heightOpt.isEmpty || weightOpt.isEmpty) {
      simpleAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter numeric values for age, height, weight.")
      return
    }
    val age = ageOpt.get; val height = heightOpt.get; val weight = weightOpt.get
    val gender = Option(genderChoice.getValue).getOrElse("Male")
    val activity = Option(activityLevelChoice.getValue).getOrElse("Active")
    val theme = Option(themeChoice.getValue).getOrElse("Light")

    // Compute calorie target (Mifflin-St Jeor)
    val bmr = gender match {
      case "Male" => 10*weight + 6.25*height - 5*age + 5
      case "Female" => 10*weight + 6.25*height - 5*age - 161
      case _ => 10*weight + 6.25*height - 5*age
    }
    val activityFactor = activity match {
      case "Sedentary" => 1.2
      case "Lightly Active" => 1.375
      case "Active" => 1.55
      case "Very Active" => 1.725
      case _ => 1.5
    }
    val maintain = bmr * activityFactor
    // Existing goal weightGoal property maybe; default maintain
    val weightGoal = goalsProps.getProperty("weightGoal", "Maintain Weight")
    val adjustment = weightGoal match {
      case "Lose Weight" => -500
      case "Gain Weight" => 300
      case _ => 0
    }
    val target = math.max(1200, math.round(maintain + adjustment).toInt)

    profileProps.setProperty("age", age.toString)
    profileProps.setProperty("gender", gender)
    profileProps.setProperty("height", height.toString)
    profileProps.setProperty("weight", weight.toString)
    profileProps.setProperty("activity", activity)
    profileProps.setProperty("theme", theme)
    val pfos = new FileOutputStream(profileConfPath); profileProps.store(pfos, null); pfos.close()

    goalsProps.setProperty("calorieTarget", target.toString)
    val gfos = new FileOutputStream(goalsConfPath); goalsProps.store(gfos, null); gfos.close()

    MainApp.refreshTheme()

    simpleAlert(Alert.AlertType.INFORMATION, "Profile Saved", s"Calorie target updated to $target kcal")
  }

  private def exportData(): Unit = {
    val chooser = new FileChooser()
    chooser.setTitle("Export Data Zip")
    chooser.getExtensionFilters.add(new FileChooser.ExtensionFilter("Zip", "*.zip"))
    val defaultName = s"calorie-data-${DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now())}.zip"
    chooser.setInitialFileName(defaultName)
    val file = chooser.showSaveDialog(null)
    if (file != null) {
      val ok = DataExportUtil.exportAll(file.getAbsolutePath)
      simpleAlert(if(ok) Alert.AlertType.INFORMATION else Alert.AlertType.ERROR, if(ok) "Export Complete" else "Export Failed", file.getAbsolutePath)
    }
  }

  private def importData(): Unit = {
    val chooser = new FileChooser()
    chooser.setTitle("Import Data Zip")
    chooser.getExtensionFilters.add(new FileChooser.ExtensionFilter("Zip", "*.zip"))
    val file = chooser.showOpenDialog(null)
    if (file != null) {
      val ok = DataExportUtil.importAll(file.getAbsolutePath)
      simpleAlert(if(ok) Alert.AlertType.INFORMATION else Alert.AlertType.ERROR, if(ok) "Import Complete" else "Import Failed", file.getAbsolutePath)
      if (ok) { loadExisting(); MainApp.refreshTheme() }
    }
  }

  private def toInt(s: String) = Try(s.trim.toInt).toOption
  private def toDouble(s: String) = Try(s.trim.toDouble).toOption

  private def simpleAlert(t: Alert.AlertType, title: String, msg: String): Unit = {
    val a = new Alert(t); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait()
  }
}
