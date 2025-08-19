package controller

import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.scene.Node
import javafx.stage.Stage

object Nav {
  private val pages = Map(
    "Home" -> "/fxml/Home.fxml",
    "Food Logging" -> "/fxml/FoodLogging.fxml",
    "Activity" -> "/fxml/ActivityTracking.fxml",
    "Goals" -> "/fxml/Goals.fxml",
    "Progress" -> "/fxml/Progress.fxml",
    "Notifications" -> "/fxml/Notifications.fxml",
    "Profile" -> "/fxml/Profile.fxml"
  )

  def switchFrom(node: Node, page: String): Unit = {
    val loader = new FXMLLoader(getClass.getResource(pages(page)))
    val root = loader.load[Parent]()
    val stage = node.getScene.getWindow.asInstanceOf[Stage]
    stage.setTitle(s"Calorie Tracker - $page")
    stage.setScene(new Scene(root, 800, 600))
  }
}

