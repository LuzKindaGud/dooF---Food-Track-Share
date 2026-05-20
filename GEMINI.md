# Gemini AI Assistant Instructions for DoAnCoSo3 (Food-Track-Share)

## Project Overview
This is an Android application named "Food-Track-Share" (package: `com.example.doancoso3`). It is designed to track food inventory, manage shopping lists, and share data among family groups.

## Tech Stack & Architecture
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Modern declarative UI). There are remnants of XML layouts, but core activities (`MainActivity`, `AuthActivity`) are driven by Compose via `setContent`.
- **Architecture:** MVVM (Model-View-ViewModel) with the Repository pattern.
- **Dependency Injection:** Dagger Hilt.
- **Local Database:** Room Database (schemas located in `app/schemas`).
- **Remote Backend:** Firebase (Authentication, Firestore, Messaging).
- **Navigation:** Jetpack Navigation Compose.
- **Background Work:** WorkManager with Hilt integration.
- **Additional Features:** Google ML Kit for Barcode Scanning.
- **Testing:** Unit testing is handled with JUnit5, Kotest, MockK, and Kotlinx Coroutines Test. UI/Integration testing utilizes Espresso and Compose Test rules.

## Project Structure
- `app/src/main/java/com/example/doancoso3/`: Root package directory.
  - `data/`: Contains `local` (Room DAOs, Database), `model` (Data classes, Entities), `remote` (Firebase interactions), and `repository` (Interfaces and Implementations for data access).
  - `di/`: Hilt dependency injection modules (e.g., `DatabaseModule`, `FirebaseModule`, `RepositoryModule`).
  - `ui/`: Compose screens, ViewModels, and navigation flows organized by feature (`auth`, `home`, `fooditem`, `group`, `shopping`, etc.).
  - `utils/`: Helper classes and validators.
- `app/src/test/`: Unit tests (Kotest, MockK).
- `app/src/androidTest/`: Instrumented tests (Espresso, Compose UI tests).
- `app/schemas/`: Room database exported schemas.

## Build and Run Commands
Standard Gradle commands apply. Use the Gradle wrapper included in the project:

- **Build Debug APK:** `./gradlew assembleDebug`
- **Run Unit Tests:** `./gradlew test` (configured to use JUnit Platform for Kotest/JUnit5)
- **Run Instrumented Tests:** `./gradlew connectedAndroidTest`
- **Clean Project:** `./gradlew clean`

## Development Conventions
1. **Compose First:** Prefer Jetpack Compose for any new UI development. The project is migrating or built with Compose as the primary UI toolkit.
2. **Dependency Injection:** All ViewModels and Repositories should be injected using Hilt (`@HiltViewModel`, `@Inject`).
3. **Data Flow:** UI should observe state from ViewModels (e.g., using `StateFlow` or `LiveData`). ViewModels communicate with Repositories. Repositories coordinate between local (Room) and remote (Firebase) data sources.
4. **Testing:** New features should include unit tests using Kotest and MockK. Test files should mirror the package structure of the code under test.
5. **Separation of Concerns:** Keep business logic out of Compose functions. Pass events up to the ViewModel and state down to the Composables.