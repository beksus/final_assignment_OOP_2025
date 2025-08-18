import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.*
import scalafx.scene.layout.*
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.beans.property.{StringProperty, ObjectProperty}

object MyApp extends JFXApp3:

  override def start(): Unit =

    val logFile = "meal_log.csv"

    // Read food data from CSV
    val foodData = ObservableBuffer.from(FileUtil.readFoodData("food_data.csv"))

    // Meal log and restore from file
    val mealLog = new MealLog()
    FileUtil.readMealLog(logFile).foreach(mealLog.addItem)

    val mealLogList = new ListView[String]()
    val totalLabel = new Label(
      f"Total: Calories ${mealLog.totalCalories} | Protein ${mealLog.totalProtein}%.1fg | Fat ${mealLog.totalFat}%.1fg | Carbs ${mealLog.totalCarbs}%.1fg"
    )
    mealLogList.items = ObservableBuffer(mealLog.getItems.map(_.name): _*)

    // Table of all food
    val foodTable = new TableView[SimpleFood](foodData):
      columns ++= List(
        new TableColumn[SimpleFood, String] {
          text = "Food"
          cellValueFactory = { cellData =>
            StringProperty(cellData.value.name)
          }
          prefWidth = 150
        },
        new TableColumn[SimpleFood, Number] {
          text = "Calories"
          cellValueFactory = { cellData =>
            ObjectProperty [Number](cellData.value.calories)
          }
        },
        new TableColumn[SimpleFood, Number] {
          text = "Protein (g)"
          cellValueFactory = { cellData =>
            ObjectProperty[Number](cellData.value.protein)
          }
        },
        new TableColumn[SimpleFood, Number] {
          text = "Fat (g)"
          cellValueFactory = { cellData =>
            ObjectProperty[Number](cellData.value.fat)
          }
        },
        new TableColumn[SimpleFood, Number] {
          text = "Carbs (g)"
          cellValueFactory = { cellData =>
            ObjectProperty[Number](cellData.value.carbs)
          }
        }
      )

    // Add to meal log button
    val addButton = new Button("Add to Meal Log"):
      onAction = _ =>
        val selected = foodTable.getSelectionModel.getSelectedItem
        if selected != null then
          mealLog.addItem(selected)
          FileUtil.appendMealLog(logFile, selected)
          mealLogList.items = ObservableBuffer(mealLog.getItems.map(_.name): _*)
          totalLabel.text = f"Total: Calories ${mealLog.totalCalories} | Protein ${mealLog.totalProtein}%.1fg | Fat ${mealLog.totalFat}%.1fg | Carbs ${mealLog.totalCarbs}%.1fg"

    val clearButton = new Button("Clear Log"):
      onAction = _ =>
        mealLog.clear()
        mealLogList.items = ObservableBuffer()
        totalLabel.text = "Total: Calories 0 | Protein 0g | Fat 0g | Carbs 0g"

    // Layout
    val leftPane = new VBox(10):
      padding = Insets(10)
      children = Seq(
        new Label("Food List"),
        foodTable,
        addButton
      )

    val rightPane = new VBox(10):
      padding = Insets(10)
      children = Seq(
        new Label("Today's Meal Log"),
        mealLogList,
        totalLabel,
        clearButton
      )

    stage = new PrimaryStage:
      title = "Nutrition Tracker"
      width = 900
      height = 600
      scene = new Scene:
        root = new HBox(20):
          padding = Insets(15)
          children = Seq(leftPane, rightPane)

end MyApp