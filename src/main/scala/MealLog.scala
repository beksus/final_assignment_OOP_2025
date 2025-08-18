class MealLog:
  private var items: List[FoodItem] = List()

  def addItem(item: FoodItem): Unit =
    items = item :: items

  def getItems: List[FoodItem] = items

  def totalCalories: Int = items.map(_.calories).sum
  def totalProtein: Double = items.map(_.protein).sum
  def totalFat: Double = items.map(_.fat).sum
  def totalCarbs: Double = items.map(_.carbs).sum

  def clear(): Unit =
    items = List()
