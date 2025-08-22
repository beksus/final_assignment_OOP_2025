package controller

import java.io.File
import java.util.Properties
import scala.util.Try
import javafx.scene.Scene

object ThemeUtil {
  private val profileConf = "src/main/resources/profile.conf"
  private def readTheme(): String = {
    val p = new Properties()
    val f = new File(profileConf)
    if (f.exists()) Try { val fis = new java.io.FileInputStream(f); try p.load(fis) finally fis.close() }
    p.getProperty("theme", "Light")
  }
  def applyTheme(scene: Scene): Unit = {
    if (scene == null) return
    val theme = readTheme().toLowerCase
    scene.getStylesheets.clear()
    val cssName = if (theme.contains("dark")) "dark.css" else "light.css"
    val url = getClass.getResource(s"/css/$cssName")
    if (url != null) scene.getStylesheets.add(url.toExternalForm)
  }
}
