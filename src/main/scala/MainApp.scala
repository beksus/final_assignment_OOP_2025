package controller

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.Includes._
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene => JFXScene}

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
  private val pagesLower = pages.map { case (k,v) => k.toLowerCase -> v }

  private var currentRoot: Parent = _

  override def start(): Unit = switchScene("Home")

  def switchScene(rawPage: String): Unit = {
    val pageKey = Option(rawPage).map(_.trim).filter(_.nonEmpty).getOrElse("Home")
    val fxml = pages.get(pageKey).orElse(pagesLower.get(pageKey.toLowerCase)).getOrElse({
      println(s"[MainApp] Unknown page '$rawPage' -> fallback Home"); pages("Home")
    })
    val loader = new FXMLLoader(getClass.getResource(fxml))
    currentRoot = loader.load[Parent]()
    val jfxScene = new JFXScene(currentRoot, 1200, 760)
    val wrappedScene = new Scene(jfxScene)
    if (stage == null) {
      stage = new JFXApp3.PrimaryStage { title = s"Calorie Tracker - $pageKey"; scene = wrappedScene }
    } else {
      stage.title = s"Calorie Tracker - $pageKey"
      stage.scene = wrappedScene
    }
    ThemeUtil.applyTheme(stage.scene.value.delegate)
    Nav.wireNav(currentRoot)
  }

  def refreshTheme(): Unit = if (stage != null && stage.scene.value != null) ThemeUtil.applyTheme(stage.scene.value.delegate)
}