package controller

import javafx.fxml.FXML
import javafx.scene.control.{TextField, ChoiceBox, Button, MenuBar}
import javafx.collections.FXCollections

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

  @FXML def initialize(): Unit = {
    if (menuBar != null) {
      menuBar.getMenus.forEach { menu =>
        menu.getItems.forEach { item =>
          item.setOnAction(_ => Nav.switchFrom(menuBar, item.getText))
        }
      }
    }
    ageField.setText("28")
    genderChoice.setItems(FXCollections.observableArrayList("Male", "Female", "Other"))
    genderChoice.setValue("Male")
    heightField.setText("175")
    weightField.setText("70")
    activityLevelChoice.setItems(FXCollections.observableArrayList("Sedentary", "Lightly Active", "Active", "Very Active"))
    activityLevelChoice.setValue("Active")
    themeChoice.setItems(FXCollections.observableArrayList("Light", "Dark"))
    themeChoice.setValue("Light")
  }
}
