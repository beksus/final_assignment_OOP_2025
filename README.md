# Calorie Tracker Pro

A comprehensive calorie tracking application built with Scala and JavaFX/ScalaFX with FXML integration.

## Features

### 📊 Dashboard / Home Page
- Daily calorie summary with progress bars
- Macro goals tracking (protein, carbs, fats)  
- Quick action buttons for food and exercise logging
- Activity stats (steps, exercise, hydration)
- Motivational messages and progress indicators

### 🍎 Food Logging Page  
- Organized meal sections (Breakfast, Lunch, Dinner, Snacks)
- Comprehensive food database search
- Barcode scanner placeholder integration
- AR/VR food recognition placeholder
- Manual serving size adjustments
- Recently used foods tracking

### 🏃 Activity Tracking Page
- Manual workout entry with exercise database
- Wearable device sync placeholders (Fitbit, Apple Watch, Google Fit, Strava)
- Real-time activity metrics (steps, distance, heart rate, active minutes)
- Calorie burn charts with multiple time periods
- Activity history and trends

### 🎯 Goals Page
- Comprehensive weight goal setting (lose, maintain, gain)
- Automatic calorie target calculator based on BMR/TDEE
- Interactive macro split customization with sliders
- Water intake and activity goal setting
- Quick preset configurations (Weight Loss, Maintenance, Muscle Gain, Athlete)

### 📈 Progress / History Page
- Weight progress tracking with line charts
- Calorie intake vs. burn trend analysis
- Detailed macronutrient breakdown charts
- Activity progress visualization
- Achievement badges and streak tracking
- Data export functionality

### 🔔 Notifications / Reminders Page
- Customizable meal reminders with time settings
- Water intake notification system
- Goal achievement alerts
- Custom reminder creation
- Do Not Disturb mode
- Multiple notification types and sounds

### ⚙️ Profile / Settings Page
- Complete user profile management
- BMI, BMR, and TDEE calculations
- Device integration management (placeholders for major fitness platforms)
- App theme and preference settings
- Privacy controls and data management
- Import/export functionality

## Technical Architecture

### Built With
- **Scala 3.3.4** - Modern, type-safe programming language
- **JavaFX 21.0.4** - Rich desktop UI framework  
- **ScalaFX 21.0.0-R32** - Scala wrapper for JavaFX
- **FXML** - Declarative UI markup for clean separation of concerns

### Project Structure
```
src/main/
├── scala/
│   ├── controllers/          # FXML Controllers for each page
│   │   ├── BaseController.scala
│   │   ├── DashboardController.scala
│   │   ├── FoodLoggingController.scala
│   │   ├── ActivityController.scala
│   │   ├── GoalsController.scala
│   │   ├── ProgressController.scala
│   │   ├── NotificationsController.scala
│   │   └── ProfileController.scala
│   ├── models/               # Data models
│   │   ├── User.scala
│   │   ├── FoodItem.scala
│   │   ├── Activity.scala
│   │   ├── DailyNutrition.scala
│   │   └── MealLog.scala
│   ├── utils/                # Utilities and data management
│   │   ├── DataManager.scala
│   │   └── FileUtil.scala
│   ├── CalorieTrackerApp.scala  # Main application
│   └── Main.scala            # Entry point
├── resources/
│   ├── fxml/                 # FXML layout files
│   │   ├── dashboard.fxml
│   │   ├── foodlogging.fxml
│   │   ├── activity.fxml
│   │   ├── goals.fxml
│   │   ├── progress.fxml
│   │   ├── notifications.fxml
│   │   └── profile.fxml
│   ├── styles.css            # Application styling
│   ├── food_data.csv         # Sample food database
│   └── meal_log.csv          # User meal logs
```

## Key Design Patterns

### Model-View-Controller (MVC)
- **Models**: Data classes for User, Food, Activity, etc.
- **Views**: FXML files defining UI layouts
- **Controllers**: Scala classes handling user interactions

### Data Management
- Centralized `DataManager` singleton for application state
- Mock data generators for demonstration purposes
- CSV-based persistence for food database and meal logs

### Navigation System
- Menu bar navigation between different app sections
- Clean page loading with proper controller lifecycle management
- Consistent styling and user experience across all pages

## Mock Features (Placeholders)

The following features are implemented as placeholders with informational dialogs:

- **Barcode Scanning**: Camera integration for product identification
- **AR/VR Food Recognition**: Computer vision for food identification and portion estimation
- **Device Syncing**: Integration with Fitbit, Apple Watch, Google Fit, Strava, MyFitnessPal
- **Advanced Analytics**: Machine learning for personalized recommendations
- **Social Features**: Sharing achievements and connecting with friends

## Running the Application

### Prerequisites
- Java 11 or higher
- SBT (Scala Build Tool)

### Build and Run
```bash
# Clone the repository
git clone [repository-url]
cd final_assignment_OOP_2025

# Compile the project
sbt compile

# Run the application
sbt run
```

### Alternative Run Methods
```bash
# Run with main class specification
sbt "runMain Main"

# Run the CalorieTrackerApp directly
sbt "runMain CalorieTrackerApp"
```

## Sample Data

The application comes with sample data including:

- **100 food items** with realistic nutritional information
- **Mock user profile** with calculated BMI, BMR, and TDEE
- **Sample activity history** for the last 7 days
- **Weight tracking data** for the last 30 days
- **Nutrition logs** with varied daily intake

## Future Enhancements

Potential areas for expansion:

1. **Real Device Integration**: Actual API connections to fitness platforms
2. **Machine Learning**: Personalized recommendations and insights
3. **Social Features**: Community challenges and sharing
4. **Mobile Companion**: Android/iOS app with synchronization
5. **Advanced Analytics**: Detailed health trend analysis
6. **Meal Planning**: AI-powered meal suggestions and shopping lists
7. **Barcode Database**: Real product database integration
8. **Cloud Sync**: Multi-device synchronization

## Contributing

This application demonstrates modern Scala application development practices including:

- Clean architecture with separation of concerns
- Type-safe programming with Scala 3
- Reactive UI programming with ScalaFX
- FXML-based declarative UI design
- Comprehensive error handling and user feedback
- Mock-driven development for complex integrations

## License

Educational project for demonstrating Scala and JavaFX application development.
