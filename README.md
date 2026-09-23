# Calorie Tracker Pro

A Scala 3 desktop application for logging food, workouts, and body weight, setting goals, and viewing progress. Built for the 2025 OOP final assignment, it uses **ScalaFX, JavaFX, and FXML** with local CSV and configuration files.

The application includes seven pages: **Home, Food Logging, Activity, Goals, Progress, Notifications, and Profile**.

> **Status:** educational desktop prototype. Core logging and visualization workflows are implemented; device integrations and scheduled notifications are not. Included food records are demonstration data.

## Features

| Page | Implemented behavior |
| --- | --- |
| **Home** | Today's calorie progress and macro chart; quick food/activity entry; manual steps, exercise, and hydration values. |
| **Food Logging** | Search the food library, select servings, log food by double-clicking, record weight, and manage meal/weight history. |
| **Activity** | Enter a workout and duration, save a calorie-burn estimate, and view daily totals in a bar chart. |
| **Goals** | Save weight-goal mode, calorie target, macro percentages, and water target. |
| **Progress** | View weight history, calorie intake and burn, aggregate macros, and logging statistics. |
| **Notifications** | Save meal-reminder selections, a reminder hour, and a goal-alert preference. |
| **Profile** | Save profile details, calculate a calorie target, choose light/dark styling, and export/import data as ZIP files. |

Barcode scanning, AR scanning, device synchronization, Google Fit, and Fitbit controls are placeholders without working integrations. Notification preferences are saved, but there is no scheduling or notification-delivery service.

## Technology

| Component | Version / format |
| --- | --- |
| Scala | 3.3.4 |
| ScalaFX | 21.0.0-R32 |
| JavaFX | 21.0.4 |
| sbt | 1.11.3 |
| Interface | FXML layouts and CSS themes |
| Persistence | CSV files and Java Properties configuration files |

Versions are defined in [build.sbt](build.sbt) and [project/build.properties](project/build.properties).

## Getting started

### Requirements

- A JDK compatible with JavaFX 21; **JDK 21** is a straightforward choice for this project.
- sbt.
- A desktop environment and internet access for the initial dependency download.

JavaFX 21 requires macOS 11 or later on macOS, and GTK 3.8 or later on Linux. See the [JavaFX 21 platform notes](https://openjfx.io/highlights/21/).

The build selects JavaFX native artifacts by operating system (`win`, `linux`, or `mac`), but does not detect CPU architecture. ARM systems may require an appropriate JavaFX classifier change in `build.sbt`.

### Clone, compile, and run

```sh
git clone https://github.com/beksus/final_assignment_OOP_2025.git
cd final_assignment_OOP_2025
sbt compile
sbt "runMain controller.MainApp"
```

Run from the **repository root**: the application reads and writes several files using paths relative to that directory. sbt downloads the configured Scala and JavaFX dependencies.

The repository also contains an earlier standalone interface, which can be launched separately:

```sh
sbt "runMain controller.myfirstscala"
```

Use `controller.MainApp` for the seven-page application. Plain `sbt run` may ask you to choose between entry points.

## Basic workflow

1. Open **Goals**, select a weight-goal mode, and save it.
2. Open **Profile**, enter your details, choose a theme, and click **Save Profile**. Saving recalculates the calorie target using the saved goal.
3. In **Food Logging**, type a food name into the search field, set the serving count, and double-click a matching result. The initial list shows recently logged foods; typing searches the complete library.
4. Use **Log Weight** to add a weight entry. Use **Manage Meals** or **Manage Weights** to edit or delete entries through dialogs.
5. In **Activity**, enter a workout name and duration. The current estimate is `duration × 8` calories, regardless of workout type.
6. View summaries in **Home** and history in **Progress**.
7. Use **Profile → Export Zip** to back up the local logs and settings.

Meals are dated with the current local date. The current log format does not store breakfast/lunch/dinner categories.

Profile calorie targets use a Mifflin–St Jeor-style calculation, activity multipliers, goal adjustments, and a code-defined lower bound. These are application estimates, not personalized nutrition recommendations. Macro charts display recorded gram totals rather than energy percentages.

## Local data

The main application stores its writable data under `src/main/resources/`:

| File | Purpose |
| --- | --- |
| `food_data.csv` | Library of 100 sample food records; loaded as a classpath resource. |
| `meal_log.csv` | Dated food entries with calories, protein, fat, and carbs. |
| `activity_log.csv` | Dated workouts, duration, and calories burned. |
| `weight_log.csv` | Dated weight measurements. |
| `goals.conf` | Weight-goal mode, calorie target, macro split, and water goal. |
| `profile.conf` | Profile details and theme. |
| `user_stats.conf` | Manually entered steps, exercise, and hydration values. |
| `notifications.conf` | Reminder and goal-alert preferences. |

The separate `meal_log.csv` at the repository root belongs to the older `controller.myfirstscala` interface.

Example schemas:

```text
meal_log.csv:     date,food_name,calories,protein,fat,carbs
activity_log.csv: date,workout,duration,calories
weight_log.csv:   date,weight
```

ZIP export includes the three logs and four configuration files above, excluding the food library. Import overwrites matching files.

There is no database, account system, or cloud synchronization. Keep the repository writable while using the app; the current storage paths are designed for running from a source checkout.

## Architecture and project structure

FXML views define the interface, Scala controllers handle events and state, and shared utilities manage navigation, themes, calculations, and persistence.

```text
build.sbt
project/build.properties
src/main/
  scala/
    MainApp.scala                  # controller.MainApp entry point
    controller/
      *Controller.scala            # Seven page controllers
      FoodItem.scala               # Abstract FoodItem and SimpleFood
      MealLog.scala                # Encapsulated food collection
      FileUtil.scala               # CSV and configuration persistence
      Nav.scala                    # Page navigation
      ThemeUtil.scala              # CSS selection
      CalorieCalculator.scala      # Standalone calculation helper
      AchievementUtil.scala        # Logging statistics
      DataExportUtil.scala         # ZIP backup and restore
      myfirstscala.scala           # Earlier standalone interface
  resources/
    fxml/                          # Seven page layouts
    css/                           # Light and dark themes
    *.csv                          # Sample data and logs
    *.conf                         # Settings
src/test/scala/controller/
  CalorieCalculatorSpec.scala
```

`FoodItem` defines an abstract nutrition interface, and `SimpleFood` implements it. `MealLog` encapsulates a private collection and exposes totals. Singleton utility objects provide shared services.

Some empty or older source files remain outside `controller/`. The committed `target/` and `project/target/` directories contain generated build artifacts rather than authoritative source.

## Tests

[CalorieCalculatorSpec.scala](src/test/scala/controller/CalorieCalculatorSpec.scala) contains three ScalaTest checks for activity-level effects, the weight-loss adjustment, and the calculation's lower bound.

However, `build.sbt` currently does **not** declare ScalaTest. Add a Scala 3-compatible `org.scalatest %% scalatest` dependency in the `Test` configuration before running:

```sh
sbt test
```

The profile controller currently duplicates the calculation logic instead of calling `CalorieCalculator`, so those tests do not directly verify the profile UI calculation.

## Current limitations

- **Sample metrics:** Activity-page steps, distance, heart rate, and active minutes are fixed examples, not sensor readings.
- **Inline editing:** meal-table edits compare newly loaded entries by object identity, so changes may not persist. Use the Manage Meals dialog instead. Inline weight edits affect all records sharing the selected date.
- **Validation:** macro sliders need not total 100%, and some numeric fields accept malformed or unrealistic values. CSV parsing does not support quoted commas in names.
- **Backups:** ZIP import does not validate entry paths or restrict them to the expected data files. Use only trusted, app-generated backups until extraction is hardened.
- **Statistics:** the displayed streak is the longest historical run of dates containing both meal and activity records, not necessarily a streak ending today.
- **Packaging:** writable paths point into the source tree; standalone packaging requires a dedicated user-data directory.

This documentation was checked against the source, FXML layouts, and build configuration. The application and tests were not executed during this review.

## License

No license file is currently included in the repository.

