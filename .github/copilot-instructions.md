# Copilot instructions

## Build, test, lint
- Build debug APK: `.\gradlew.bat :app:assembleDebug`
- Unit tests: `.\gradlew.bat :app:testDebugUnitTest`
- Run a single unit test: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.doancoso3.utils.AuthValidatorPropertyTest"`
- Instrumented tests: `.\gradlew.bat :app:connectedDebugAndroidTest`
- Lint: `.\gradlew.bat :app:lintDebug`

## High-level architecture
- Single Android app module (`:app`) built with Jetpack Compose + Material 3, Hilt DI, Room, and Firebase.
- App entry points are Compose-based:
  - `FoodShareTrackApplication` enables Hilt.
  - `AuthActivity` hosts `AuthComposeScreen` with Compose Navigation for login/register/forgot flows.
  - `MainActivity` hosts `MainComposeScreen` with bottom-tab navigation and `HomeComposeScreen`.
- Data layer:
  - Room database `AppDatabase` with entities in `data.model` and DAOs in `data.local.dao`.
  - Firebase Auth/Firestore/Messaging are provided via `FirebaseModule`.
  - `AuthRepository` + `AuthRepositoryImpl` wrap Firebase Auth/Firestore for auth flows.
- Legacy XML/Fragment flows also exist (`nav_auth.xml`, `nav_main.xml`, and `ui/auth` + `ui/home` fragments). They are not wired to the current activities but remain in the codebase.
- Auth flow is documented in `docs/auth_activity_diagram.puml`.

## Specs and roadmap
- Product requirements, design, and the implementation task list live in `.kiro/specs/food-share-track/{requirements,design,tasks}.md`.
- The design doc calls for an offline-first MVVM + Repository architecture (Room as source of truth, Firestore as sync target), WorkManager for background expiry/sync, ML Kit barcode scanning, and FCM notifications—align new work with those specs.

## Key conventions
- Hilt is the DI backbone: modules live under `di`, ViewModels use `@HiltViewModel`, Activities/Fragments use `@AndroidEntryPoint`, Compose screens obtain VMs via `hiltViewModel()`.
- Prefer Jetpack Compose for new UI and keep UI changes aligned with existing feature logic to avoid conflicts or duplication.
- UI state is exposed with `LiveData` in ViewModels and observed via `observeAsState()` in Compose (or `observe()` in fragments).
- Auth input validation is centralized in `AuthValidator`; `AuthViewModel` maps validation and Firebase errors to per-field LiveData.
- Room schema export is enabled via KSP (`room.schemaLocation`), and the DB builder uses `fallbackToDestructiveMigration(dropAllTables = true)` (schema changes wipe local data).
- Unit tests run on JUnit Platform (Kotest is in use); add new JVM tests under `app/src/test`.
