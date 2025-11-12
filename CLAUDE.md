# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android XR application built on the "Hello Android XR" sample, demonstrating spatial computing features for Android XR devices. The app implements an Instagram-like social media interface adapted for XR environments with features including home feed, stories, reels, search, messages, and profile management.

**Package Name:** `com.appbuildchat.instaxr`

**Target Platform:** Android XR (requires Android Studio Canary with XR emulator)

## Build & Development Commands

### Build Commands
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug APK to connected device/emulator
./gradlew installDebug
```

### Running & Testing
```bash
# Run on connected XR emulator
# Note: Use Android Studio's Run button or manually install via:
./gradlew installDebug

# Sync Gradle
./gradlew sync
```

## Project Architecture

This project follows **Android's recommended layered architecture** with clear separation of concerns as documented in `app/ANDROID_ARCHITECTURE_GUIDELINES.md`. The codebase is currently in a single-module structure with plans to potentially expand to multi-module.

### Architecture Layers

```
┌─────────────────────────────────────┐
│          UI Layer                   │
│  (Compose Screens + ViewModels)    │
├─────────────────────────────────────┤
│          Data Layer                 │
│  (Repositories + Firebase)          │
├─────────────────────────────────────┤
│        XR Integration Layer         │
│  (Environment Controller)           │
└─────────────────────────────────────┘
```

### Key Architectural Patterns

1. **UI Layer** (`ui/` package)
   - Feature-based organization: Each feature (home, search, messages, etc.) has its own package
   - Each feature contains: `<Feature>Screen.kt` and `<Feature>ViewModel.kt`
   - Uses Jetpack Compose for declarative UI
   - ViewModels use `StateFlow` for state management
   - All ViewModels are annotated with `@HiltViewModel` for DI

2. **Data Layer** (`data/` package)
   - Repository pattern with interface-first approach
   - `data/model/` contains domain models (User, Post, Story, Reel, Message, Chat)
   - `data/repository/` contains repository interfaces and implementations
   - Firebase Firestore used as backend (repositories currently have TODO stubs)
   - Repositories follow naming: `<Feature>Repository` interface, `Default<Feature>Repository` implementation

3. **XR Integration** (`environment/` package)
   - `EnvironmentController` manages XR spatial environment features
   - Handles space modes (Home Space vs Full Space)
   - Manages custom 3D environments and passthrough
   - Uses AndroidX XR SceneCore APIs

4. **Dependency Injection** (`di/` package)
   - Hilt for dependency injection throughout
   - `FirebaseModule.kt` provides Firebase instances
   - `RepositoryModule.kt` binds repository implementations
   - Application class: `InstaXRApplication` annotated with `@HiltAndroidApp`

### Navigation

Navigation is centralized in `ui/Navigation.kt`:
- Uses Jetpack Navigation Compose
- Route constants defined in `AppRoutes` object
- Main routes: HOME, MY_PAGE, SEARCH, MESSAGES, SETTINGS, ADD_POST
- Custom extension: `NavHostController.navigateSingleTopTo()` for single-top navigation with state preservation

## Technology Stack

### Core Dependencies
- **Android XR Libraries**
  - `androidx.xr.arcore` - ARCore integration
  - `androidx.xr.scenecore` - 3D scene management
  - `androidx.xr.compose` - XR Compose components
  - `com.android.extensions.xr` - XR extensions

- **UI Framework**
  - Jetpack Compose (Material3)
  - Compose BOM version: 2025.08.00
  - Kotlin 2.1.0 with Compose compiler plugin

- **Architecture Components**
  - Lifecycle (ViewModel, Runtime) 2.9.3
  - Navigation Compose 2.9.3
  - Hilt 2.51.1 (Dependency Injection)
  - KSP 2.1.0-1.0.29

- **Backend**
  - Firebase BOM 33.7.0
  - Firebase Firestore (database)
  - Firebase Storage (media)
  - Firebase Analytics

### Gradle Configuration

- Build configuration uses **Kotlin DSL (KTS)** for all Gradle files
- **Version Catalog** (`gradle/libs.versions.toml`) centralizes all dependency versions
- Minimum SDK: 24, Target SDK: 36, Compile SDK: 36
- Java compatibility: VERSION_1_8

## XR-Specific Development

### Environment Modes
The app supports three environment presentation modes via `EnvironmentController`:
1. **Home Space Mode** - Panels appear in user's physical space
2. **Full Space Mode** - Immersive 3D environment
3. **Passthrough** - Transparent AR view of physical world

### XR Manifest Configuration
The AndroidManifest includes:
```xml
<uses-feature android:name="android.software.xr.api.spatial" android:required="false" />
<property android:name="android.window.PROPERTY_XR_ACTIVITY_START_MODE"
          android:value="XR_ACTIVITY_START_MODE_HOME_SPACE_MANAGED" />
```

### Loading 3D Models
Use `EnvironmentController.loadModelAsset(modelName)` to preload GLTF models into asset cache before applying as spatial environments.

## Current Development State

### Implemented Features
- Navigation structure with 5 main screens (Home, Search, Messages, Profile, Settings)
- Basic UI components for XR (TextPane, EnvironmentControls, SearchBar, VerticalSidePanel, ContentDetailPanel, Toolbox)
- Data models for social features (User, Post, Story, Reel, Message, Chat)
- Repository interfaces with DI setup
- XR environment management system
- Theme system with Material3

### Pending Implementation (TODO markers in code)
- All repository methods are stubs - Firebase integration needs implementation
- AddPostScreen not created yet (routes to HomeScreen)
- Actual data fetching and persistence logic
- Authentication flow
- Media upload/download functionality

## Important Development Notes

1. **Android Studio Version**: Must use **Android Studio Canary** (not stable release) to access XR emulator and APIs. Download from: https://developer.android.com/studio/preview

2. **XR Emulator Setup**: Install the latest XR emulator image through SDK Manager before running the app.

3. **Firebase Configuration**: The project includes Firebase dependencies but requires a `google-services.json` file (not in repo) for full Firebase functionality.

4. **Architecture Guidelines**: When adding new features, follow the comprehensive patterns documented in `app/ANDROID_ARCHITECTURE_GUIDELINES.md` which covers:
   - Dependency setup with version catalog
   - Folder structure conventions
   - UI Layer patterns (Screens, ViewModels, Navigation)
   - Data Layer patterns (Repositories, DAOs, DTOs)
   - Naming conventions
   - Best practices for Compose, StateFlow, and Hilt

5. **Code Style**: The project uses standard Kotlin conventions and follows the unidirectional data flow pattern with reactive programming using Kotlin Flow.

6. **Testing**: No test files currently exist. When adding tests, create unit tests in `app/src/test/` and instrumented tests in `app/src/androidTest/`.

## Project Structure

```
app/src/main/java/com/appbuildchat/instaxr/
├── InstaXRApplication.kt          # Hilt application entry point
├── MainActivity.kt                # Single activity host
├── ui/
│   ├── InstaXRApp.kt             # Root composable
│   ├── Navigation.kt             # Navigation graph & routes
│   ├── home/                     # Home feed feature
│   ├── search/                   # Search feature
│   ├── messages/                 # Messaging feature
│   ├── profile/                  # User profile feature
│   ├── settings/                 # Settings feature
│   ├── stories/                  # Stories feature
│   ├── reels/                    # Reels feature
│   ├── components/               # Reusable UI components
│   ├── shared/                   # Shared XR UI components
│   └── theme/                    # Material3 theming
├── data/
│   ├── model/                    # Domain models
│   └── repository/               # Repository interfaces & implementations
├── environment/
│   └── EnvironmentController.kt  # XR environment management
└── di/                           # Hilt dependency injection modules
```

## Resources & Documentation

- Official Android XR Documentation: https://developer.android.com/develop/xr
- Android XR Design Guidelines: https://developer.android.com/design/ui/xr
- Android XR Bootcamp: https://developer.android.com/develop/xr#bootcamp
- Architecture Guide: `app/ANDROID_ARCHITECTURE_GUIDELINES.md` (comprehensive 1000+ line reference)
- XR Design Principles: `guidelines/` folder contains detailed XR UX guidelines

## Git Branch Information

- **Main branch**: `main`
- **Current feature branch**: `feat/search-screen`
- Recent work includes: navigation setup, launch icon, Hilt integration, Firebase dependencies, and initial project structure