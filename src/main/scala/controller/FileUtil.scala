package controller

import controller.FileUtil.getClass

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import scala.io.Source

object FileUtil:

  def readFoodData(resourceName: String): List[SimpleFood] =
    val stream = getClass.getResourceAsStream(s"/$resourceName")
    if stream == null then throw new Exception(s"Resource not found: $resourceName")

    val source = Source.fromInputStream(stream)(StandardCharsets.UTF_8)
    val lines = source.getLines().drop(1)
    val foodList = lines.map { line =>
      val Array(name, cal, pro, fat, carbs) = line.split(",").map(_.trim)
      new SimpleFood(name, cal.toInt, pro.toDouble, fat.toDouble, carbs.toDouble)
    }.toList
    source.close()
    foodList

  // Writes to normal writable path (NOT resources)
  def appendMealLog(path: String, food: FoodItem): Unit =
    val file = new File(path)
    val bw = new BufferedWriter(new FileWriter(file, true))
    try
      if !file.exists() || file.length() == 0 then
        bw.write("date,food_name,calories,protein,fat,carbs")
        bw.newLine()
      val today = LocalDate.now()
      val line = s"$today,${food.name},${food.calories},${food.protein},${food.fat},${food.carbs}"
      bw.write(line)
      bw.newLine()
    finally
      bw.close()

  def readMealLog(path: String): List[FoodItem] =
    if !File(path).exists() then return List()
    val source = Source.fromFile(path)
    val lines = source.getLines().drop(1)
    val list = lines.map { line =>
      val Array(_, name, cal, pro, fat, carbs) = line.split(",").map(_.trim)
      new SimpleFood(name, cal.toInt, pro.toDouble, fat.toDouble, carbs.toDouble)
    }.toList
    source.close()
    list
