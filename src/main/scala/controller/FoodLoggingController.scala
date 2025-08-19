package controller

import javafx.fxml.FXML
import javafx.scene.control.{TextField, Button, TabPane, Spinner, ListView, Label, Tab, MenuBar}
import javafx.collections.FXCollections
import javafx.scene.input.KeyEvent
import javafx.scene.control.ListCell
import javafx.util.Callback
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType

class FoodLoggingController {
  @FXML private var menuBar: MenuBar = _
  @FXML private var searchBar: TextField = _
  @FXML private var barcodeBtn: Button = _
  @FXML private var arvrBtn: Button = _
  @FXML private var mealTabs: TabPane = _
  @FXML private var servingSizeSpinner: Spinner[Integer] = _
  @FXML private var recentFoodsList: ListView[String] = _

  private var allFoods: List[SimpleFood] = Nil
  private val mealLogPath = "src/main/resources/meal_log.csv"

  @FXML def initialize(): Unit = {
    // Wire top MenuBar navigation
    if (menuBar != null) {
      menuBar.getMenus.forEach { menu =>
        menu.getItems.forEach { item =>
          item.setOnAction(_ => Nav.switchFrom(menuBar, item.getText))
        }
      }
    }
    // Load all foods from CSV
    allFoods = FileUtil.readFoodData("food_data.csv")
    val foodNames = FXCollections.observableArrayList(allFoods.map(_.name): _*)
    recentFoodsList.setItems(foodNames)
    recentFoodsList.setCellFactory(_ => new ListCell[String] {
      override def updateItem(item: String, empty: Boolean): Unit = {
        super.updateItem(item, empty)
        setText(if (empty || item == null) "" else item)
      }
    })
    // Search bar listener
    searchBar.setOnKeyReleased((_: KeyEvent) => {
      val query = Option(searchBar.getText).getOrElse("").toLowerCase
      val filtered = allFoods.filter(_.name.toLowerCase.contains(query)).map(_.name)
      recentFoodsList.setItems(FXCollections.observableArrayList(filtered: _*))
    })
    // Add food on double click
    recentFoodsList.setOnMouseClicked(event => {
      if (event.getClickCount == 2) {
        val selectedName = recentFoodsList.getSelectionModel.getSelectedItem
        val selectedFood = allFoods.find(_.name == selectedName)
        selectedFood.foreach { sf =>
          val servings = Option(servingSizeSpinner.getValue).map(_.intValue()).getOrElse(1)
          val foodToLog = new SimpleFood(
            sf.name,
            sf.calories * servings,
            sf.protein * servings,
            sf.fat * servings,
            sf.carbs * servings
          )
          FileUtil.appendMealLog(mealLogPath, foodToLog)
          val alert = new Alert(AlertType.INFORMATION)
          alert.setTitle("Food Logged")
          alert.setHeaderText(null)
          alert.setContentText(s"Logged ${servings}x ${sf.name} to meal log.")
          alert.showAndWait()
        }
      }
    })
    // Show recently used foods
    val recentFoods = FileUtil.readMealLog(mealLogPath).map(_.name).distinct.reverse.take(10)
    recentFoodsList.setItems(FXCollections.observableArrayList(recentFoods: _*))
    if (servingSizeSpinner.getValueFactory != null) servingSizeSpinner.getValueFactory.setValue(1)
  }
}