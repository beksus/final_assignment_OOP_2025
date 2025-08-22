package controller

import java.io.{File, FileInputStream, FileOutputStream, BufferedInputStream, BufferedOutputStream}
import java.util.zip.{ZipEntry, ZipOutputStream, ZipInputStream}
import scala.util.Try

object DataExportUtil {
  private val dataFiles = Seq(
    "meal_log.csv",
    "activity_log.csv",
    "weight_log.csv",
    "goals.conf",
    "profile.conf",
    "user_stats.conf",
    "notifications.conf"
  )
  private val base = "src/main/resources/"

  def exportAll(zipPath: String): Boolean = Try {
    val zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipPath)))
    try
      dataFiles.foreach { name =>
        val f = new File(base + name)
        if (f.exists()) {
          zos.putNextEntry(new ZipEntry(name))
          val in = new BufferedInputStream(new FileInputStream(f))
          try
            val buf = new Array[Byte](4096)
            Iterator.continually(in.read(buf)).takeWhile(_ != -1).foreach(read => zos.write(buf,0,read))
          finally in.close()
          zos.closeEntry()
        }
      }
    finally zos.close()
  }.isSuccess

  def importAll(zipPath: String): Boolean = Try {
    val zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipPath)))
    try
      Iterator
        .continually(zis.getNextEntry)
        .takeWhile(_ != null)
        .foreach { entry =>
          val outFile = new File(base + entry.getName)
          outFile.getParentFile.mkdirs()
          val out = new BufferedOutputStream(new FileOutputStream(outFile))
          try
            val buf = new Array[Byte](4096)
            Iterator.continually(zis.read(buf)).takeWhile(_ != -1).foreach(read => out.write(buf,0,read))
          finally out.close()
          zis.closeEntry()
        }
    finally zis.close()
  }.isSuccess
}

