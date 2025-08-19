package controller

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.Includes._
import scalafx.stage.Stage
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.MenuItem
import javafx.scene.control.Button

/**
 * Main application entry point for Calorie Tracker. Loads FXML pages and handles navigation via menu bar.
 */
object MainApp extends JFXApp3 {
  private val pages = Map(
    "Home" -> "/fxml/Home.fxml",
    "Food Logging" -> "/fxml/FoodLogging.fxml",
    "Activity" -> "/fxml/ActivityTracking.fxml",
    "Goals" -> "/fxml/Goals.fxml",
    "Progress" -> "/fxml/Progress.fxml",
    "Notifications" -> "/fxml/Notifications.fxml",
    "Profile" -> "/fxml/Profile.fxml"
  )

  private var currentRoot: Parent = _

  override def start(): Unit = {
    switchScene("Home")
  }

  // Make public so controllers can navigate
  def switchScene(page: String): Unit = {
    val loader = new FXMLLoader(getClass.getResource(pages(page)))
    currentRoot = loader.load[Parent]()
    stage = new JFXApp3.PrimaryStage {
      title = s"Calorie Tracker - $page"
      scene = new Scene(currentRoot, 800, 600)
    }
    val navBar = currentRoot.lookup("#navBar").asInstanceOf[javafx.scene.layout.HBox]
    if (navBar != null) {
      navBar.getChildren.forEach { node =>
        node match {
          case btn: Button =>
            btn.setOnAction(_ => switchScene(btn.getText))
          case _ =>
        }
      }
    }
  }
}
