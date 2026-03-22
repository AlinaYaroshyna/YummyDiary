# YummyDiary

An Android application that holds all your meals and recipes. It provides a menu-driven interface for managing a meal diary, adding new dishes, browsing all recipes, viewing a meal map, and learning about the authors.

---

## Table of Contents

- [Project Structure](#project-structure)
- [Technologies & Frameworks](#technologies--frameworks)
- [Dependencies](#dependencies)
- [Architecture](#architecture)
- [Key Source Files](#key-source-files)
- [Resources](#resources)
- [Testing](#testing)
- [Build & Run](#build--run)

---

## Project Structure

```
YummyDiary/
├── app/
│   ├── build.gradle.kts              # App-level Gradle configuration
│   ├── proguard-rules.pro            # R8/ProGuard minification rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml   # App manifest (activities, permissions)
│       │   ├── java/com/example/yummydiary/
│       │   │   ├── MainActivity.kt   # Compose-based main entry point
│       │   │   ├── MenuActivity.kt   # XML-based menu screen
│       │   │   ├── MenuItem.kt       # Data class for menu items
│       │   │   ├── MenuAdapter.kt    # RecyclerView adapter for menu list
│       │   │   ├── FirstFragment.kt  # Navigation fragment 1
│       │   │   ├── SecondFragment.kt # Navigation fragment 2
│       │   │   └── ui/theme/
│       │   │       ├── Color.kt      # Material 3 color definitions
│       │   │       ├── Theme.kt      # App theme (light/dark, dynamic color)
│       │   │       └── Type.kt       # Typography definitions
│       │   └── res/
│       │       ├── layout/           # XML layouts for activities & fragments
│       │       ├── navigation/       # nav_graph.xml (fragment navigation)
│       │       ├── drawable/         # Vector drawables and launcher icons
│       │       ├── mipmap-*/         # App icons at multiple screen densities
│       │       ├── values/           # Colors, strings, dimensions, themes
│       │       ├── values-night/     # Dark-mode resource overrides
│       │       ├── values-land/      # Landscape resource overrides
│       │       ├── values-v23/       # API 23+ resource overrides
│       │       ├── values-w600dp/    # Tablet (600 dp+) resource overrides
│       │       └── values-w1240dp/   # Large screen (1240 dp+) resource overrides
│       ├── test/                     # JUnit unit tests
│       └── androidTest/              # Espresso instrumented tests
├── gradle/
│   ├── libs.versions.toml            # Centralized version catalog
│   └── wrapper/                      # Gradle wrapper binaries & config
├── build.gradle.kts                  # Root Gradle build configuration
├── settings.gradle.kts               # Project/module settings & repositories
├── gradle.properties                 # Gradle JVM arguments & Android flags
└── local.properties                  # Local SDK path (machine-specific, git-ignored)
```

---

## Technologies & Frameworks

| Technology | Version | Purpose |
|---|---|---|
| **Kotlin** | 2.0.21 | Primary programming language |
| **Android SDK** | API 24 – 36 | Mobile platform (min Android 7.1, target Android 15) |
| **Jetpack Compose** | BOM 2024.09.00 | Modern declarative UI toolkit |
| **Material Design 3** | via Compose BOM | UI design system and components |
| **AndroidX Core KTX** | 1.10.1 | Kotlin extensions for Android Core APIs |
| **AndroidX Lifecycle** | 2.6.1 | Lifecycle-aware components |
| **AndroidX Activity Compose** | 1.8.0 | Compose integration with Activity |
| **Android Navigation** | via `nav_graph.xml` | Fragment-based in-app navigation |
| **RecyclerView** | via AndroidX | Efficient list/grid rendering |
| **JUnit 4** | 4.13.2 | Unit testing framework |
| **Espresso** | 3.5.1 | UI instrumented testing framework |
| **Android Gradle Plugin (AGP)** | 9.0.1 | Android build system |
| **Gradle** | 9.2.1 | Dependency management and build automation |
| **Java** | 11 | JVM compilation target |

---

## Dependencies

All dependency versions are centrally managed in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

### Runtime Dependencies

| Artifact | Version | Description |
|---|---|---|
| `androidx.core:core-ktx` | 1.10.1 | Kotlin-idiomatic extensions for Android Core |
| `androidx.lifecycle:lifecycle-runtime-ktx` | 2.6.1 | Lifecycle-aware coroutine and LiveData helpers |
| `androidx.activity:activity-compose` | 1.8.0 | `setContent {}` and Compose Activity integration |
| `androidx.compose:compose-bom` | 2024.09.00 | Bill of Materials that aligns all Compose library versions |
| `androidx.compose.ui:ui` | (via BOM) | Core Compose UI primitives |
| `androidx.compose.ui:ui-graphics` | (via BOM) | Graphics and drawing utilities for Compose |
| `androidx.compose.ui:ui-tooling-preview` | (via BOM) | `@Preview` support in Android Studio |
| `androidx.compose.material3:material3` | (via BOM) | Material You (Material 3) components and theming |

### Debug-Only Dependencies

| Artifact | Version | Description |
|---|---|---|
| `androidx.compose.ui:ui-tooling` | (via BOM) | Full Compose tooling (layout inspector, etc.) |
| `androidx.compose.ui:ui-test-manifest` | (via BOM) | Test manifest for Compose test runner |

### Test Dependencies

| Artifact | Version | Description |
|---|---|---|
| `junit:junit` | 4.13.2 | JUnit 4 for local unit tests |
| `androidx.test.ext:junit` | 1.1.5 | AndroidX JUnit extensions for instrumented tests |
| `androidx.test.espresso:espresso-core` | 3.5.1 | Espresso UI testing on device/emulator |
| `androidx.compose.ui:ui-test-junit4` | (via BOM) | Compose UI testing with JUnit 4 |

### Gradle Plugins

| Plugin | Version | Description |
|---|---|---|
| `com.android.application` | 9.0.1 | Android application build support |
| `org.jetbrains.kotlin.plugin.compose` | 2.0.21 | Kotlin compiler plugin for Jetpack Compose |

---

## Architecture

The app uses a **multi-activity + fragment** architecture with a mix of traditional XML layouts and modern Jetpack Compose:

- **MainActivity** — Compose-based entry point; applies `YummyDiaryTheme` and renders the main screen.
- **MenuActivity** — XML-based activity; uses a `RecyclerView` with `MenuAdapter` to display five menu options:
  - Dziennik dań *(Meal Diary)*
  - Dodaj nowe danie *(Add New Meal)*
  - Mapa dań *(Meal Map)*
  - Wszystkie przepisy *(All Recipes)*
  - O autorach *(About Authors)*
- **FirstFragment / SecondFragment** — Fragment-based screens wired together via `nav_graph.xml` using the AndroidX Navigation component.

---

## Key Source Files

| File | Role |
|---|---|
| `MainActivity.kt` | Compose entry point; edge-to-edge layout with Material 3 Scaffold |
| `MenuActivity.kt` | Hosts a `RecyclerView` menu with `LinearLayoutManager` |
| `MenuItem.kt` | `data class MenuItem(title: String, icon: Int)` |
| `MenuAdapter.kt` | RecyclerView adapter; binds `MenuItem` data to views, forwards click events |
| `FirstFragment.kt` | Fragment with ViewBinding; navigates to `SecondFragment` |
| `SecondFragment.kt` | Fragment with ViewBinding; navigates back to `FirstFragment` |
| `ui/theme/Color.kt` | Purple/PurpleGrey/Pink color tokens for light and dark schemes |
| `ui/theme/Theme.kt` | `YummyDiaryTheme` composable; supports dynamic color (Android 12+) and dark mode |
| `ui/theme/Type.kt` | Material 3 `Typography` with `bodyLarge` style (16 sp) |

---

## Resources

| Directory | Contents |
|---|---|
| `res/layout/` | `activity_menu.xml`, `item_menu.xml`, `fragment_first.xml`, `fragment_second.xml`, `content_main.xml` |
| `res/navigation/` | `nav_graph.xml` — navigation graph connecting First ↔ Second fragments |
| `res/values/` | `colors.xml`, `strings.xml`, `dimens.xml`, `themes.xml` |
| `res/values-night/` | Dark-mode theme overrides |
| `res/drawable/` | Vector drawables for the launcher icon |
| `res/mipmap-*/` | Launcher icons at `mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi` densities |

---

## Testing

### Unit Tests (`src/test/`)

- Framework: **JUnit 4**
- Location: `app/src/test/java/com/example/yummydiary/`
- Example: `ExampleUnitTest.kt` — verifies basic arithmetic

```bash
./gradlew test
```

### Instrumented Tests (`src/androidTest/`)

- Framework: **Espresso** + **AndroidJUnit4**
- Location: `app/src/androidTest/java/com/example/yummydiary/`
- Example: `ExampleInstrumentedTest.kt` — verifies app package name on a device/emulator

```bash
./gradlew connectedAndroidTest
```

---

## Build & Run

**Prerequisites:** Android SDK, JDK 11+, Android device or emulator (API 24+).

```bash
# Build a debug APK
./gradlew assembleDebug

# Install and launch on a connected device/emulator
./gradlew installDebug

# Build a release APK
./gradlew assembleRelease

# Clean the build directory
./gradlew clean

# Run all unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Print the full dependency tree
./gradlew dependencies

# Run Android Lint checks
./gradlew lint
```

Open the project in **Android Studio** by selecting *File → Open* and pointing to the `YummyDiary` folder. Gradle will sync automatically.
