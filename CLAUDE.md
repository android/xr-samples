# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android XR sample application demonstrating an Instagram-like social media experience in mixed reality. The app showcases spatial UI capabilities including Spatial Panels, Orbiters, and dynamic environment management. Built using Jetpack Compose, the app supports both traditional 2D mode and immersive XR spatial mode.

**Key Technologies:**
- Android XR SDK (androidx.xr.*)
- Jetpack Compose for declarative UI
- Hilt for dependency injection
- Firebase (Firestore, Storage, Analytics)
- Kotlin Coroutines & Flow
- Coil for image loading
- Navigation Compose

## Build & Development Commands

```bash
# Build the app
./gradlew assembleDebug

# Clean build
./gradlew clean

# Install to device/emulator
./gradlew installDebug

# Or use ADB directly
adb install -r app/build/outputs/apk/debug/app-debug.apk

# View logs
adb logcat | grep "InstaXR"

# Launch app
adb shell am start -n com.appbuildchat.instaxr/.MainActivity
```

**Environment Requirements:**
- Android Studio Canary (latest version recommended for XR support)
- Android SDK 36 (compileSdk)
- Minimum SDK 24
- XR emulator image from SDK Manager
- JDK 1.8

## Architecture

This project follows Android's recommended architecture with a single-module structure:

```
app/src/main/java/com/appbuildchat/instaxr/
├── InstaXRApplication.kt       # Hilt application
├── MainActivity.kt             # Single activity entry point
├── ui/                         # UI Layer (Screens + ViewModels)
│   ├── InstaXRApp.kt          # Main composable with XR/2D mode switching
│   ├── Navigation.kt          # Navigation graph with AppRoutes
│   ├── home/                  # Home feed with spatial panel expansion
│   ├── search/                # Search screen
│   ├── messages/              # Messages/chat screen
│   ├── profile/               # User profile screen
│   ├── settings/              # Settings screen
│   ├── reels/                 # Reels/stories screen
│   ├── shared/                # Shared spatial UI components
│   ├── components/            # Reusable UI components
│   └── theme/                 # Material3 theme configuration
├── data/                       # Data Layer
│   ├── model/                 # Data models (Post, User, Comment, etc.)
│   ├── repository/            # Repository interfaces and implementations
│   └── local/                 # Local data source (MockDataLoader)
├── di/                        # Dependency injection modules
│   ├── FirebaseModule.kt      # Firebase dependencies
│   └── RepositoryModule.kt    # Repository bindings
└── environment/               # XR Environment management
    └── EnvironmentController.kt
```

### Core Architectural Patterns

1. **Dual-Mode UI System**: The app dynamically switches between 2D (Home Space) and XR (Full Space) modes
   - `InstaXRApp.kt` checks `LocalSpatialCapabilities.current.isSpatialUiEnabled`
   - XR mode uses `ApplicationSubspace` wrapper for spatial UI
   - 2D mode uses traditional Compose layouts

2. **Activity-Scoped ViewModels**: HomeViewModel is scoped to Activity level to share state between `InstaXRApp` and `HomeScreen`
   - Required for coordinating spatial panel expansion/collapse
   - Use `hiltViewModel(viewModelStoreOwner = activity)` pattern

3. **Spatial Panel States**: Home screen has two distinct states:
   - **Collapsed**: Single SpatialPanel (680dp width) showing posts list
   - **Expanded**: Three SpatialPanels (compact list, image detail, comments) with animated transitions
   - Transition triggered by selecting a post via `HomeAction.SelectPost`

4. **Navigation with Orbiters**: Bottom navigation uses XR Orbiters (floating UI) attached to panel edges
   - Position: `ContentEdge.Bottom`
   - Alignment options: `Alignment.CenterHorizontally`, `Alignment.End`

## XR-Specific Implementation Details

### Spatial UI Components
- **SpatialPanel**: Main container for 2D content in 3D space (requires SubspaceModifier for sizing)
- **Orbiter**: Floating UI elements attached to panel edges (navigation bar, buttons)
- **ApplicationSubspace**: Top-level spatial context wrapper (required for SpatialPanels)
- **Subspace**: Creates spatial context for grouping multiple SpatialPanels
- **SpatialRow**: Layout container for arranging panels horizontally in space

### Critical XR Rules
1. Always wrap spatial UI in `ApplicationSubspace` at the top level
2. Use `Subspace` when creating multiple related SpatialPanels
3. Use `SubspaceModifier.width()/.height()` for panel sizing (not regular Modifier)
4. SpatialPanels require `MovePolicy` and `ResizePolicy` configuration
5. Check `LocalSpatialCapabilities.current.isSpatialUiEnabled` before rendering spatial UI

### Environment Management
`EnvironmentController.kt` handles XR scene environments:
- `requestHomeSpaceMode()`: Switch to 2D mode
- `requestFullSpaceMode()`: Switch to XR mode
- `requestPassthrough()`: Enable passthrough view
- `requestCustomEnvironment(name)`: Load custom 3D environments
- `loadModelAsset(name)`: Preload GLTF models for environments

## Data Layer Implementation

The app currently uses a **mock data layer** with plans for Firebase integration:

- **Repositories**: Interface-based pattern (e.g., `PostRepository` with `DefaultPostRepository`)
- **Firebase Setup**: Firestore and Storage configured but not fully implemented (marked with TODO comments)
- **Mock Data**: `MockDataLoader.kt` provides sample data for development
- **Flow-Based**: All repository methods expose `Flow<T>` for reactive updates

**Important**: When implementing Firebase data fetching, update repository implementations in `data/repository/` and remove mock data calls from ViewModels.

## Navigation Structure

Routes defined in `AppRoutes` object (Navigation.kt):
- `HOME` - Main feed with spatial expansion capability
- `SEARCH` - Search users and content
- `MESSAGES` - Chat/messaging interface
- `MY_PAGE` - User profile
- `SETTINGS` - App settings
- `ADD_POST` - Post creation (TODO: not implemented)

Use `navController.navigateSingleTopTo(route)` extension for safe navigation with state preservation.

## Dependency Injection

Hilt modules in `di/` package:
- **FirebaseModule**: Provides Firebase instances (Firestore, Storage, Analytics)
- **RepositoryModule**: Binds repository interfaces to implementations

All ViewModels use `@HiltViewModel` annotation and constructor injection.

## Testing Notes

- Test runner configured: `androidx.test.runner.AndroidJUnitRunner`
- No custom test runner or test modules currently implemented
- When adding tests, follow patterns from `ANDROID_ARCHITECTURE_GUIDELINES.md`

## Common Development Tasks

### Adding a New Feature Screen

1. Create feature package under `ui/<feature>/`
2. Implement `<Feature>Screen.kt` (composable) and `<Feature>ViewModel.kt`
3. Add route to `AppRoutes` object in `Navigation.kt`
4. Add composable destination in `AppNavigation` NavHost
5. Add navigation item to bottom bar in `InstaXRApp.kt` (both spatial and 2D modes)
6. If feature needs data, create repository in `data/repository/`

### Modifying Spatial Panel Layout

Main spatial logic is in `InstaXRApp.kt` and `home/HomeScreen.kt`:
- Single panel state: `SpatialPanel` with width/height modifiers
- Multi-panel state: `SpatialRow` containing multiple `SpatialPanel` instances
- Panel animations: Use `AnimatedVisibility` with `expandHorizontally`/`shrinkHorizontally`
- State coordination: HomeViewModel manages `selectedPost` state

### Adding XR Environments

1. Add GLTF model to assets
2. Call `EnvironmentController.loadModelAsset(modelName)` in initialization
3. Trigger with `EnvironmentController.requestCustomEnvironment(modelName)`
4. Example: SettingsScreen can add environment switcher UI

## Important Conventions

- **Package naming**: Feature-based organization under `ui/`
- **ViewModel state**: Use sealed interfaces for UI state (e.g., `HomeUiState.Loading/Success/Error`)
- **Actions**: Define sealed interfaces/classes for user actions (e.g., `HomeAction`)
- **Hilt scoping**: Default ViewModels are Composable-scoped; use Activity-scoped when sharing state
- **Compose best practices**: Follow guidelines in `ANDROID_ARCHITECTURE_GUIDELINES.md`

## Known Issues & TODOs

- `AddPostScreen` not implemented (currently navigates to HomeScreen)
- Firebase repository implementations are placeholders (TODOs in code)
- No actual data persistence or network calls yet
- Test infrastructure not fully set up

## Additional Resources

- README.md: Project overview and Android XR documentation links
- ANDROID_ARCHITECTURE_GUIDELINES.md: Comprehensive Android architecture patterns
- Android XR Documentation: https://developer.android.com/develop/xr
