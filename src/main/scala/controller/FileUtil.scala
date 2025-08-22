package controller

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import scala.io.Source
import scala.util.Try

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
    val file = new File(path)
    if !file.exists() then
      return List()
    val source = Source.fromFile(file)
    val lines = source.getLines().drop(1)
    val list = lines.map { line =>
      val Array(_, name, cal, pro, fat, carbs) = line.split(",").map(_.trim)
      new SimpleFood(name, cal.toInt, pro.toDouble, fat.toDouble, carbs.toDouble)
    }.toList
    source.close()
    list

  // Weight log utilities
  def appendWeightLog(path: String, weight: Double): Unit =
    val file = new File(path)
    val bw = new BufferedWriter(new FileWriter(file, true))
    try
      if !file.exists() || file.length() == 0 then
        bw.write("date,weight")
        bw.newLine()
      val today = LocalDate.now()
      bw.write(s"$today,$weight")
      bw.newLine()
    finally
      bw.close()

  def readWeightLog(path: String): List[(String, Double)] =
    val file = new File(path)
    if !file.exists() then return Nil
    val source = Source.fromFile(file)
    val entries = source.getLines().drop(1).flatMap { line =>
      val parts = line.split(",").map(_.trim)
      if parts.length == 2 && parts(1).matches("[0-9.]+") then Some(parts(0) -> parts(1).toDouble) else None
    }.toList
    source.close()
    entries

  case class MealEntry(date: String, name: String, calories: Int, protein: Double, fat: Double, carbs: Double)

  def readMealEntries(path: String): List[MealEntry] =
    val file = new File(path)
    if !file.exists() then return Nil
    val src = Try(Source.fromFile(file)).toOption
    src match
      case None => Nil
      case Some(source) =>
        val entries = source.getLines().drop(1).flatMap { line =>
          val parts = line.split(",").map(_.trim)
          if parts.length == 6 && parts(2).forall(_.isDigit) then
            Try(MealEntry(parts(0), parts(1), parts(2).toInt, parts(3).toDouble, parts(4).toDouble, parts(5).toDouble)).toOption
          else None
        }.toList
        source.close()
        entries

  def writeMealEntries(path: String, entries: List[MealEntry]): Unit =
    val file = new File(path)
    val bwTry = Try(new BufferedWriter(new FileWriter(file, false)))
    bwTry.foreach { bw =>
      try
        bw.write("date,food_name,calories,protein,fat,carbs")
        bw.newLine()
        entries.foreach { e =>
          bw.write(s"${e.date},${e.name},${e.calories},${e.protein},${e.fat},${e.carbs}")
          bw.newLine()
        }
      finally bw.close()
    }

  def writeWeightLog(path: String, entries: List[(String, Double)]): Unit =
    val file = new File(path)
    val bwTry = Try(new BufferedWriter(new FileWriter(file, false)))
    bwTry.foreach { bw =>
      try
        bw.write("date,weight")
        bw.newLine()
        entries.foreach { case (d, w) =>
          bw.write(s"$d,$w")
          bw.newLine()
        }
      finally bw.close()
    }

  def loadProperties(path: String): java.util.Properties =
    val p = new java.util.Properties()
    val f = new File(path)
    if f.exists() then
      Try { val fis = new java.io.FileInputStream(f); try p.load(fis) finally fis.close() }
    p

  def saveProperties(path: String, props: java.util.Properties): Unit =
    val f = new File(path)
    Try {
      val fos = new java.io.FileOutputStream(f)
      try props.store(fos, null) finally fos.close()
    }
