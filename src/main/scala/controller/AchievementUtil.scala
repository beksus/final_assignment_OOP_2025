package controller

import java.time.LocalDate
import scala.util.Try

object AchievementUtil {
  def consecutiveStreak(dates: Iterable[String]): Int = {
    val parsed = dates.flatMap(d=>Try(LocalDate.parse(d)).toOption).toList.sorted
    if (parsed.isEmpty) return 0
    var max=1; var cur=1
    for (i <- 1 until parsed.length) {
      if (parsed(i).minusDays(1)==parsed(i-1)) { cur+=1; if (cur>max) max=cur } else cur=1
    }
    max
  }
  case class GoalStats(streak:Int, loggedDays:Int, intakeGoalDays:Int)
  def computeStats(intakeMap: Map[String,Int], burnDays:Set[String], calorieTarget:Int): GoalStats = {
    val intakeDays = intakeMap.keySet
    val streak = consecutiveStreak(intakeDays.intersect(burnDays))
    val goalDays = intakeMap.count{case(_,c)=> c <= calorieTarget}
    GoalStats(streak,intakeDays.size,goalDays)
  }
}
