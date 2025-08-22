package controller

import javafx.scene.{Parent, Node}
import javafx.scene.control.{Button, ToggleButton, MenuBar}

object Nav {
  /** Switch by delegating to MainApp to keep a single source of truth */
  def switchFrom(node: Node, rawPage: String): Unit = {
    val page = Option(rawPage).map(_.trim).filter(_.nonEmpty).getOrElse("Home")
    MainApp.switchScene(page)
  }

  /** Wire navigation buttons (Button or ToggleButton) and optional menu bar */
  def wireNav(root: Parent): Unit = {
    val navBar = root.lookup("#navBar")
    if (navBar != null) {
      val children = navBar.asInstanceOf[javafx.scene.layout.HBox].getChildren
      children.forEach {
        case b: Button => b.setOnAction(_ => switchFrom(b, b.getText))
        case tb: ToggleButton => tb.setOnAction(_ => switchFrom(tb, tb.getText))
        case _ => ()
      }
    }
    root.lookup("#menuBar") match {
      case mb: MenuBar =>
        mb.getMenus.forEach { menu =>
          menu.getItems.forEach { item => item.setOnAction(_ => switchFrom(mb, item.getText)) }
        }
      case _ => ()
    }
  }
}
