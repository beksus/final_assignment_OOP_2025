package controller

import javafx.fxml.FXML
import javafx.scene.control.{TextField, Button, TabPane, Spinner, ListView, MenuBar, Alert, TextInputDialog, TableView, TableColumn, cell}
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.collections.{FXCollections, ObservableList}
import javafx.util.StringConverter
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.input.KeyEvent
import javafx.scene.control.ListCell
import javafx.scene.control.Alert.AlertType
import java.util.Optional

class FoodLoggingController {
  @FXML private var menuBar: MenuBar = _
  @FXML private var searchBar: TextField = _
  @FXML private var barcodeBtn: Button = _
  @FXML private var arvrBtn: Button = _
  @FXML private var mealTabs: TabPane = _
  @FXML private var servingSizeSpinner: Spinner[Integer] = _
  @FXML private var recentFoodsList: ListView[String] = _
  @FXML private var viewLogBtn: Button = _ // new button to view logged items
  @FXML private var logWeightBtn: Button = _ // new button to log weight
  @FXML private var manageMealsBtn: Button = _
  @FXML private var manageWeightsBtn: Button = _
  @FXML private var mealTable: TableView[FileUtil.MealEntry] = _
  @FXML private var mealDateCol: TableColumn[FileUtil.MealEntry, String] = _
  @FXML private var mealNameCol: TableColumn[FileUtil.MealEntry, String] = _
  @FXML private var mealCalCol: TableColumn[FileUtil.MealEntry, String] = _
  @FXML private var mealProCol: TableColumn[FileUtil.MealEntry, String] = _
  @FXML private var mealFatCol: TableColumn[FileUtil.MealEntry, String] = _
  @FXML private var mealCarbCol: TableColumn[FileUtil.MealEntry, String] = _
  @FXML private var deleteMealBtn: Button = _
  @FXML private var refreshMealsBtn: Button = _
  @FXML private var weightTable: TableView[(String, Double)] = _
  @FXML private var weightDateCol: TableColumn[(String, Double), String] = _
  @FXML private var weightValueCol: TableColumn[(String, Double), String] = _
  @FXML private var deleteWeightBtn: Button = _
  @FXML private var refreshWeightsBtn: Button = _

  private var allFoods: List[SimpleFood] = Nil
  private val mealLogPath = "src/main/resources/meal_log.csv"
  private val weightLogPath = "src/main/resources/weight_log.csv"

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
          refreshRecent()
        }
      }
    })
    // View log button
    if (viewLogBtn != null) {
      viewLogBtn.setOnAction(_ => showLoggedItems())
    }
    // Log weight button
    if (logWeightBtn != null) {
      logWeightBtn.setOnAction(_ => logWeight())
    }
    // Manage meals button
    if (manageMealsBtn != null) manageMealsBtn.setOnAction(_ => manageMeals())
    // Manage weights button
    if (manageWeightsBtn != null) manageWeightsBtn.setOnAction(_ => manageWeights())
    // Table and button setup
    setupMealTable()
    setupWeightTable()
    loadMealTable()
    loadWeightTable()
    if (deleteMealBtn!=null) deleteMealBtn.setOnAction(_ => deleteSelectedMeal())
    if (refreshMealsBtn!=null) refreshMealsBtn.setOnAction(_ => loadMealTable())
    if (deleteWeightBtn!=null) deleteWeightBtn.setOnAction(_ => deleteSelectedWeight())
    if (refreshWeightsBtn!=null) refreshWeightsBtn.setOnAction(_ => loadWeightTable())
    // Show recently used foods
    refreshRecent()
    if (servingSizeSpinner != null && servingSizeSpinner.getValueFactory != null) servingSizeSpinner.getValueFactory.setValue(1)
  }

  private def setupMealTable(): Unit = if (mealTable != null) {
    mealDateCol.setCellValueFactory(data => new SimpleStringProperty(data.getValue.date))
    mealNameCol.setCellValueFactory(data => new SimpleStringProperty(data.getValue.name))
    mealCalCol.setCellValueFactory(d => new SimpleStringProperty(d.getValue.calories.toString))
    mealProCol.setCellValueFactory(d => new SimpleStringProperty(d.getValue.protein.toString))
    mealFatCol.setCellValueFactory(d => new SimpleStringProperty(d.getValue.fat.toString))
    mealCarbCol.setCellValueFactory(d => new SimpleStringProperty(d.getValue.carbs.toString))
    val numericConv = new StringConverter[String] { def toString(v:String)=v; def fromString(s:String)=s }
    Seq(mealCalCol, mealProCol, mealFatCol, mealCarbCol).foreach { col =>
      col.setCellFactory(_ => new TextFieldTableCell[FileUtil.MealEntry,String](numericConv))
    }
    // commit edits -> rewrite line
    mealCalCol.setOnEditCommit(ev => updateMealNumeric(ev.getRowValue, calories = Some(toDoubleSafe(ev.getNewValue).getOrElse(ev.getRowValue.calories.toDouble).toInt)))
    mealProCol.setOnEditCommit(ev => updateMealNumeric(ev.getRowValue, protein = Some(toDoubleSafe(ev.getNewValue).getOrElse(ev.getRowValue.protein))))
    mealFatCol.setOnEditCommit(ev => updateMealNumeric(ev.getRowValue, fat = Some(toDoubleSafe(ev.getNewValue).getOrElse(ev.getRowValue.fat))))
    mealCarbCol.setOnEditCommit(ev => updateMealNumeric(ev.getRowValue, carbs = Some(toDoubleSafe(ev.getNewValue).getOrElse(ev.getRowValue.carbs))))
  }

  private def setupWeightTable(): Unit = if (weightTable != null) {
    weightDateCol.setCellValueFactory(d => new SimpleStringProperty(d.getValue._1))
    weightValueCol.setCellValueFactory(d => new SimpleStringProperty(d.getValue._2.toString))
    val conv = new StringConverter[String]{ def toString(v:String)=v; def fromString(s:String)=s }
    weightValueCol.setCellFactory(_ => new TextFieldTableCell[(String,Double),String](conv))
    weightValueCol.setOnEditCommit(ev => {
      val (date, _) = ev.getRowValue
      val newVal = toDoubleSafe(ev.getNewValue).getOrElse(ev.getRowValue._2)
      val updated = FileUtil.readWeightLog(weightLogPath).map { case (d,w) => if (d==date) d -> newVal else d -> w }
      FileUtil.writeWeightLog(weightLogPath, updated)
      loadWeightTable()
    })
  }

  private def loadMealTable(): Unit = if (mealTable != null) {
    val entries = FileUtil.readMealEntries(mealLogPath)
    mealTable.setItems(FXCollections.observableArrayList(entries*));
  }
  private def loadWeightTable(): Unit = if (weightTable != null) {
    val entries = FileUtil.readWeightLog(weightLogPath)
    weightTable.setItems(FXCollections.observableArrayList(entries*));
  }

  private def refreshRecent(): Unit = {
    val recentFoods = FileUtil.readMealLog(mealLogPath).map(_.name).distinct.reverse.take(10)
    recentFoodsList.setItems(FXCollections.observableArrayList(recentFoods: _*))
  }

  private def showLoggedItems(): Unit = {
    val entries = FileUtil.readMealEntries(mealLogPath)
    if (entries.isEmpty) {
      val alert = new Alert(AlertType.INFORMATION)
      alert.setTitle("Meal Log")
      alert.setHeaderText("No items logged yet")
      alert.setContentText("Double click a food to add it or use Quick Add features.")
      alert.showAndWait()
    } else {
      val grouped = entries.groupBy(_.date).toSeq.sortBy(_._1).reverse
      val lines = grouped.flatMap { case (date, list) =>
        val dayTotal = list.map(_.calories).sum
        val header = s"$date (Total: ${dayTotal} kcal)"
        val items = list.zipWithIndex.map { case (e, idx) => s"  - ${idx + 1}. ${e.name} ${e.calories} kcal P:${e.protein} F:${e.fat} C:${e.carbs}" }
        header +: items
      }
      val alert = new Alert(AlertType.INFORMATION)
      alert.setTitle("Meal Log")
      alert.setHeaderText("Logged Items (Grouped by Date)")
      val content = if (lines.length > 200) (lines.take(200) :+ s"... (${lines.length - 200} more)").mkString("\n") else lines.mkString("\n")
      alert.setContentText(content)
      alert.getDialogPane.setPrefWidth(520)
      alert.showAndWait()
    }
  }

  private def logWeight(): Unit = {
    val dialog = new TextInputDialog()
    dialog.setTitle("Log Weight")
    dialog.setHeaderText("Enter your current weight (kg)")
    dialog.setContentText("e.g. 70.4")
    val result: Optional[String] = dialog.showAndWait()
    if (result.isPresent) {
      val value = result.get.trim
      if (value.matches("[0-9]+(\\.[0-9]+)?")) {
        FileUtil.appendWeightLog(weightLogPath, value.toDouble)
        val alert = new Alert(AlertType.INFORMATION)
        alert.setTitle("Weight Logged")
        alert.setHeaderText(null)
        alert.setContentText(s"Logged weight: $value kg")
        alert.showAndWait()
      } else {
        val alert = new Alert(AlertType.ERROR)
        alert.setTitle("Invalid Input")
        alert.setHeaderText("Weight not logged")
        alert.setContentText("Please enter a numeric weight, e.g. 70 or 70.5")
        alert.showAndWait()
      }
    }
  }

  private def manageMeals(): Unit = {
    val entries = FileUtil.readMealEntries(mealLogPath)
    if (entries.isEmpty) {
      val a = new Alert(Alert.AlertType.INFORMATION)
      a.setTitle("Manage Meals")
      a.setHeaderText("No meals logged")
      a.setContentText("Log some meals first.")
      a.showAndWait(); return
    }
    val listing = entries.zipWithIndex.map{case(e,i)=> s"${i+1}. ${e.date} ${e.name} ${e.calories}kcal P:${e.protein} F:${e.fat} C:${e.carbs}"}.mkString("\n")
    val prompt = new TextInputDialog()
    prompt.setTitle("Manage Meals")
    prompt.setHeaderText("Enter index to delete or e<index> to edit. Current entries:")
    prompt.setContentText(listing.take(800))
    val res = prompt.showAndWait()
    if (!res.isPresent) return
    val in = res.get.trim
    if (in.matches("e[0-9]+")) {
      val idx = in.drop(1).toInt -1
      if (idx>=0 && idx<entries.length) {
        val edit = new TextInputDialog(s"${entries(idx).calories},${entries(idx).protein},${entries(idx).fat},${entries(idx).carbs}")
        edit.setTitle("Edit Meal Macros")
        edit.setHeaderText("Enter calories,protein,fat,carbs")
        edit.setContentText("e.g. 250,20,5,30")
        val er = edit.showAndWait()
        if (er.isPresent) {
          val parts = er.get.split(",").map(_.trim)
          if (parts.length==4 && parts.forall(p=>p.matches("[0-9.]+"))) {
            val updated = entries.updated(idx, entries(idx).copy(calories=parts(0).toInt, protein=parts(1).toDouble, fat=parts(2).toDouble, carbs=parts(3).toDouble))
            FileUtil.writeMealEntries(mealLogPath, updated)
            refreshRecent()
          } else simpleError("Invalid input format")
        }
      }
    } else if (in.matches("[0-9]+")) {
      val idx = in.toInt -1
      if (idx>=0 && idx<entries.length) {
        val updated = entries.patch(idx, Nil, 1)
        FileUtil.writeMealEntries(mealLogPath, updated)
        refreshRecent()
      }
    }
  }

  private def manageWeights(): Unit = {
    val entries = FileUtil.readWeightLog(weightLogPath)
    if (entries.isEmpty) {
      val a = new Alert(Alert.AlertType.INFORMATION)
      a.setTitle("Manage Weights")
      a.setHeaderText("No weights logged")
      a.setContentText("Log weight first.")
      a.showAndWait(); return
    }
    val listing = entries.zipWithIndex.map{case((d,w),i)=> s"${i+1}. $d $w kg"}.mkString("\n")
    val prompt = new TextInputDialog()
    prompt.setTitle("Manage Weights")
    prompt.setHeaderText("Enter index to delete or e<index> to edit. Current entries:")
    prompt.setContentText(listing.take(800))
    val res = prompt.showAndWait()
    if (!res.isPresent) return
    val in = res.get.trim
    if (in.matches("e[0-9]+")) {
      val idx = in.drop(1).toInt -1
      if (idx>=0 && idx<entries.length) {
        val edit = new TextInputDialog(entries(idx)._2.toString)
        edit.setTitle("Edit Weight")
        edit.setHeaderText("Enter new weight (kg)")
        val er = edit.showAndWait()
        if (er.isPresent && er.get.matches("[0-9]+(\\.[0-9]+)?")) {
          val updated = entries.updated(idx, entries(idx)._1 -> er.get.toDouble)
          FileUtil.writeWeightLog(weightLogPath, updated)
        } else if (er.isPresent) simpleError("Invalid number")
      }
    } else if (in.matches("[0-9]+")) {
      val idx = in.toInt -1
      if (idx>=0 && idx<entries.length) {
        val updated = entries.patch(idx, Nil, 1)
        FileUtil.writeWeightLog(weightLogPath, updated)
      }
    }
  }

  private def deleteSelectedMeal(): Unit = if (mealTable!=null) {
    val sel = mealTable.getSelectionModel.getSelectedItem
    if (sel!=null) {
      val remain = FileUtil.readMealEntries(mealLogPath).filterNot(_ == sel)
      FileUtil.writeMealEntries(mealLogPath, remain)
      loadMealTable(); refreshRecent()
    }
  }
  private def deleteSelectedWeight(): Unit = if (weightTable!=null) {
    val sel = weightTable.getSelectionModel.getSelectedItem
    if (sel!=null) {
      val remain = FileUtil.readWeightLog(weightLogPath).filterNot(_ == sel)
      FileUtil.writeWeightLog(weightLogPath, remain)
      loadWeightTable()
    }
  }

  private def updateMealNumeric(entry: FileUtil.MealEntry, calories:Option[Int]=None, protein:Option[Double]=None, fat:Option[Double]=None, carbs:Option[Double]=None): Unit = {
    val updated = FileUtil.readMealEntries(mealLogPath).map { e =>
      if (e eq entry) e.copy(
        calories = calories.getOrElse(e.calories),
        protein = protein.getOrElse(e.protein),
        fat = fat.getOrElse(e.fat),
        carbs = carbs.getOrElse(e.carbs)) else e
    }
    FileUtil.writeMealEntries(mealLogPath, updated)
    loadMealTable(); refreshRecent()
  }

  private def toDoubleSafe(s:String) = scala.util.Try(s.trim.toDouble).toOption

  private def simpleError(msg:String):Unit = {
    val a = new Alert(Alert.AlertType.ERROR); a.setTitle("Error"); a.setHeaderText(msg); a.showAndWait()
  }
}