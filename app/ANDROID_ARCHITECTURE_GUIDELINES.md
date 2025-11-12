# Android App Architecture Guidelines
## UI-Data Structure Organization

This guide provides best practices for organizing an Android application following a layered architecture with clear separation between UI and Data layers.

> **📝 INSTRUCTION FOR FUTURE SELF:**
>
> When creating a new Android project using this guide:
> 1. Start with the [Dependencies Setup](#dependencies-setup) section first
> 2. Set up the folder structure based on project size (single vs multi-module)
> 3. Follow the UI Layer and Data Layer patterns for each feature
> 4. Use the code examples as templates for your implementations
> 5. Always implement dependency injection with Hilt from the start
> 6. Write tests alongside your feature implementation
> 7. Refer to the [Best Practices](#best-practices) section before committing code
>
> This document is based on Google's official Android Architecture Templates and follows the [Guide to app architecture](https://developer.android.com/topic/architecture).

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Dependencies Setup](#dependencies-setup)
3. [Folder Structure](#folder-structure)
4. [UI Layer](#ui-layer)
5. [Data Layer](#data-layer)
6. [Module Organization](#module-organization)
7. [Best Practices](#best-practices)
8. [Naming Conventions](#naming-conventions)

---

## Architecture Overview

The architecture follows Android's recommended layered approach:

```
┌─────────────────────────────────────┐
│          UI Layer                   │
│  (Views + ViewModels)              │
├─────────────────────────────────────┤
│        Domain Layer (Optional)      │
│         (Use Cases)                 │
├─────────────────────────────────────┤
│          Data Layer                 │
│  (Repositories + Data Sources)     │
└─────────────────────────────────────┘
```

**Key Principles:**
- Unidirectional data flow
- Separation of concerns
- Single source of truth
- Reactive programming with Kotlin Flow

---

## Dependencies Setup

### Technology Stack

This architecture uses the following core technologies:

- **[Room Database](https://developer.android.com/training/data-storage/room)** - Local data persistence
- **[Hilt](https://developer.android.com/training/dependency-injection/hilt-android)** - Dependency injection
- **[Jetpack ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)** - UI state management
- **[Jetpack Compose](https://developer.android.com/jetpack/compose)** - Modern declarative UI
- **[Material3](https://developer.android.com/jetpack/androidx/releases/compose-material3)** - Material Design components
- **[Jetpack Navigation](https://developer.android.com/jetpack/compose/navigation)** - Navigation component
- **[Kotlin Coroutines and Flow](https://developer.android.com/kotlin/coroutines)** - Asynchronous programming
- **[KTS Gradle files](https://docs.gradle.org/current/userguide/kotlin_dsl.html)** - Build configuration
- **[Version Catalog](https://docs.gradle.org/current/userguide/platforms.html)** - Dependency management

### Gradle Setup

#### 1. Version Catalog (`gradle/libs.versions.toml`)

Create a version catalog to centralize dependency versions:

```toml
[versions]
androidGradlePlugin = "8.12.2"
androidxCore = "1.17.0"
androidxLifecycle = "2.9.3"
androidxActivity = "1.10.1"
androidxComposeBom = "2025.08.01"
androidxHilt = "1.2.0"
androidxNavigation = "2.9.3"
androidxRoom = "2.7.2"
androidxTest = "1.7.0"
androidxTestExt = "1.3.0"
androidxTestRunner = "1.7.0"
coroutines = "1.10.2"
hilt = "2.57.1"
junit = "4.13.2"
kotlin = "2.2.10"
ksp = "2.2.10-2.0.2"

[libraries]
# Core Android dependencies
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "androidxCore" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "androidxActivity" }
androidx-lifecycle-runtime-ktx = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "androidxLifecycle" }
androidx-lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "androidxLifecycle" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "androidxLifecycle" }

# Compose
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "androidxComposeBom" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3"}
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui"}
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview"}
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4"}
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling"}
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest"}

# Navigation
androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "androidxNavigation" }
androidx-hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "androidxHilt" }

# Room Database
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "androidxRoom" }
androidx-room-ktx = { module = "androidx.room:room-ktx", version.ref = "androidxRoom" }
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "androidxRoom" }

# Hilt Dependency Injection
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-android-compiler = { module = "com.google.dagger:hilt-android-compiler", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }
hilt-gradle-plugin = { module = "com.google.dagger:hilt-android-gradle-plugin", version.ref = "hilt" }

# Testing
junit = { module = "junit:junit", version.ref = "junit" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
androidx-test-core = { module = "androidx.test:core", version.ref = "androidxTest" }
androidx-test-ext-junit = { module = "androidx.test.ext:junit", version.ref = "androidxTestExt" }
androidx-test-runner = { module = "androidx.test:runner", version.ref = "androidxTestRunner" }
hilt-android-testing = { module = "com.google.dagger:hilt-android-testing", version.ref = "hilt" }

[plugins]
android-application = { id = "com.android.application", version.ref = "androidGradlePlugin" }
android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-kapt = { id = "org.jetbrains.kotlin.kapt", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp"}
hilt-gradle = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

#### 2. App Module Build Configuration (`app/build.gradle.kts`)

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.gradle)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.yourcompany.yourapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.yourcompany.yourapp"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.yourcompany.yourapp.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Enable Room auto-migrations
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        aidl = false
        buildConfig = false
        renderScript = false
        shaders = false
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    // Hilt and instrumented tests
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.android.compiler)
    // Hilt and Robolectric tests
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.android.compiler)

    // Arch Components
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Tooling
    debugImplementation(libs.androidx.compose.ui.tooling)
    // Instrumented tests
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Local tests: jUnit, coroutines, Android runner
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // Instrumented tests: jUnit rules and runners
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
```

#### 3. Project-level Build Configuration (`build.gradle.kts`)

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt.gradle) apply false
    alias(libs.plugins.ksp) apply false
}
```

#### 4. Optional: Network Dependencies

If your app needs network capabilities, add these to your version catalog:

```toml
[versions]
retrofit = "2.9.0"
okhttp = "4.12.0"
kotlinxSerialization = "1.6.3"

[libraries]
retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
retrofit-kotlin-serialization = { module = "com.squareup.retrofit2:converter-kotlinx-serialization", version.ref = "retrofit" }
okhttp-logging = { module = "com.squareup.okhttp3:logging-interceptor", version.ref = "okhttp" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerialization" }

[plugins]
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

Then add to `app/build.gradle.kts`:

```kotlin
dependencies {
    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
}
```

---

## Folder Structure

### Single Module Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/yourcompany/yourapp/
│   │   │   ├── YourApplication.kt
│   │   │   ├── ui/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── Navigation.kt
│   │   │   │   ├── feature1/
│   │   │   │   │   ├── Feature1Screen.kt
│   │   │   │   │   └── Feature1ViewModel.kt
│   │   │   │   ├── feature2/
│   │   │   │   │   ├── Feature2Screen.kt
│   │   │   │   │   └── Feature2ViewModel.kt
│   │   │   │   └── theme/
│   │   │   │       ├── Color.kt
│   │   │   │       ├── Theme.kt
│   │   │   │       └── Type.kt
│   │   │   ├── data/
│   │   │   │   ├── repository/
│   │   │   │   │   ├── UserRepository.kt
│   │   │   │   │   └── ProductRepository.kt
│   │   │   │   ├── local/
│   │   │   │   │   ├── database/
│   │   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   │   ├── UserEntity.kt
│   │   │   │   │   │   └── UserDao.kt
│   │   │   │   │   └── di/
│   │   │   │   │       └── DatabaseModule.kt
│   │   │   │   ├── remote/
│   │   │   │   │   ├── api/
│   │   │   │   │   │   └── ApiService.kt
│   │   │   │   │   ├── dto/
│   │   │   │   │   │   └── UserDto.kt
│   │   │   │   │   └── di/
│   │   │   │   │       └── NetworkModule.kt
│   │   │   │   └── di/
│   │   │   │       └── DataModule.kt
│   │   │   └── domain/ (optional)
│   │   │       ├── model/
│   │   │       │   └── User.kt
│   │   │       └── usecase/
│   │   │           └── GetUserUseCase.kt
│   │   └── res/
│   ├── test/
│   │   └── java/com/yourcompany/yourapp/
│   │       ├── data/
│   │       │   └── DefaultUserRepositoryTest.kt
│   │       └── ui/
│   │           └── feature1/
│   │               └── Feature1ViewModelTest.kt
│   └── androidTest/
│       └── java/com/yourcompany/yourapp/
│           ├── HiltTestRunner.kt
│           ├── ui/
│           │   ├── NavigationTest.kt
│           │   └── feature1/
│           │       └── Feature1ScreenTest.kt
│           └── testdi/
│               └── TestDatabaseModule.kt
```

### Multi-Module Structure

```
project/
├── app/
│   └── src/main/java/com/yourcompany/yourapp/
│       ├── YourApplication.kt
│       └── ui/
│           ├── MainActivity.kt
│           └── Navigation.kt
├── feature-feature1/
│   └── src/main/java/com/yourcompany/yourapp/feature/feature1/
│       └── ui/
│           ├── Feature1Screen.kt
│           └── Feature1ViewModel.kt
├── feature-feature2/
│   └── src/main/java/com/yourcompany/yourapp/feature/feature2/
│       └── ui/
│           ├── Feature2Screen.kt
│           └── Feature2ViewModel.kt
├── core-data/
│   └── src/main/java/com/yourcompany/yourapp/core/data/
│       ├── repository/
│       │   ├── UserRepository.kt
│       │   └── ProductRepository.kt
│       └── di/
│           └── DataModule.kt
├── core-database/
│   └── src/main/java/com/yourcompany/yourapp/core/database/
│       ├── AppDatabase.kt
│       ├── UserEntity.kt
│       ├── UserDao.kt
│       └── di/
│           └── DatabaseModule.kt
├── core-network/
│   └── src/main/java/com/yourcompany/yourapp/core/network/
│       ├── api/
│       │   └── ApiService.kt
│       ├── dto/
│       │   └── UserDto.kt
│       └── di/
│           └── NetworkModule.kt
├── core-domain/
│   └── src/main/java/com/yourcompany/yourapp/core/domain/
│       ├── model/
│       │   └── User.kt
│       └── repository/
│           └── UserRepository.kt (interface)
├── core-ui/
│   └── src/main/java/com/yourcompany/yourapp/core/ui/
│       ├── Color.kt
│       ├── Theme.kt
│       ├── Type.kt
│       └── components/
│           └── CommonButton.kt
└── core-testing/
    └── src/main/java/com/yourcompany/yourapp/core/testing/
        └── HiltTestRunner.kt
```

---

## UI Layer

The UI layer displays application data and handles user interactions.

### Components

#### 1. Screens (Composables)

**Location:** `ui/<feature>/<Feature>Screen.kt`

**Purpose:** Contains the UI composables that render the interface.

**Structure:**
```kotlin
@Composable
fun FeatureScreen(
    modifier: Modifier = Modifier,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        is FeatureUiState.Loading -> LoadingScreen()
        is FeatureUiState.Success -> FeatureContent(
            data = (uiState as FeatureUiState.Success).data,
            onAction = viewModel::handleAction,
            modifier = modifier
        )
        is FeatureUiState.Error -> ErrorScreen()
    }
}

@Composable
internal fun FeatureContent(
    data: List<Item>,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // UI implementation
}

// Preview
@Preview(showBackground = true)
@Composable
private fun FeatureContentPreview() {
    YourAppTheme {
        FeatureContent(
            data = listOf(/* sample data */),
            onAction = {}
        )
    }
}
```

**Best Practices:**
- Create a top-level composable that connects to the ViewModel
- Extract a separate internal composable for the actual UI (easier to test and preview)
- Use `collectAsStateWithLifecycle()` to observe state
- Always include `@Preview` composables with sample data
- Pass callbacks as parameters, not the entire ViewModel

#### 2. ViewModels

**Location:** `ui/<feature>/<Feature>ViewModel.kt`

**Purpose:** Manages UI state and business logic, acts as a bridge between UI and data layer.

**Structure:**
```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val repository: FeatureRepository
) : ViewModel() {

    val uiState: StateFlow<FeatureUiState> = repository
        .dataStream
        .map<List<Item>, FeatureUiState> { FeatureUiState.Success(it) }
        .catch { emit(FeatureUiState.Error(it)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FeatureUiState.Loading
        )

    fun handleAction(input: String) {
        viewModelScope.launch {
            repository.performAction(input)
        }
    }
}

sealed interface FeatureUiState {
    object Loading : FeatureUiState
    data class Error(val throwable: Throwable) : FeatureUiState
    data class Success(val data: List<Item>) : FeatureUiState
}
```

**Best Practices:**
- Use `@HiltViewModel` for dependency injection
- Expose UI state as `StateFlow` (immutable)
- Use sealed interfaces/classes for UI state variants
- Transform data layer models to UI models if needed
- Handle errors within the ViewModel
- Use `viewModelScope` for coroutines
- Use `WhileSubscribed(5000)` to stop upstream flows 5 seconds after the last subscriber
- Never pass Android framework dependencies (Context, Resources) to ViewModel

#### 3. Navigation

**Location:** `ui/Navigation.kt`

**Purpose:** Defines app navigation graph.

**Structure:**
```kotlin
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = "feature1") {
        composable("feature1") {
            Feature1Screen()
        }
        composable("feature2/{id}") { backStackEntry ->
            Feature2Screen(
                id = backStackEntry.arguments?.getString("id")
            )
        }
    }
}
```

#### 4. Theme

**Location:** `ui/theme/`

**Files:**
- `Color.kt` - Color definitions
- `Theme.kt` - Theme setup
- `Type.kt` - Typography definitions

---

## Data Layer

The data layer exposes application data and contains business logic.

### Components

#### 1. Repository

**Location:** `data/repository/<Feature>Repository.kt` or `data/<Feature>Repository.kt`

**Purpose:** Single source of truth, coordinates data from multiple sources.

**Structure:**
```kotlin
interface FeatureRepository {
    val items: Flow<List<Item>>
    suspend fun addItem(item: Item)
    suspend fun refreshItems()
}

class DefaultFeatureRepository @Inject constructor(
    private val localDataSource: FeatureDao,
    private val remoteDataSource: ApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FeatureRepository {

    override val items: Flow<List<Item>> =
        localDataSource.observeItems()
            .map { entities -> entities.map { it.toExternalModel() } }
            .flowOn(ioDispatcher)

    override suspend fun addItem(item: Item) = withContext(ioDispatcher) {
        localDataSource.insertItem(item.toEntity())
    }

    override suspend fun refreshItems() = withContext(ioDispatcher) {
        val remoteItems = remoteDataSource.fetchItems()
        localDataSource.insertAll(remoteItems.map { it.toEntity() })
    }
}
```

**Best Practices:**
- Define an interface for the repository
- Use constructor injection
- Expose data as `Flow` for reactive updates
- Handle data source coordination
- Map data layer models to domain/UI models
- Use appropriate dispatchers

#### 2. Local Data Source (Database)

**Location:** `data/local/database/`

**Files:**
- `AppDatabase.kt` - Room database definition
- `<Feature>Entity.kt` - Entity classes
- `<Feature>Dao.kt` - Data Access Objects

**Structure:**

**Entity:**
```kotlin
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

**DAO:**
```kotlin
@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY createdAt DESC")
    fun observeItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Int): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}
```

**Database:**
```kotlin
@Database(
    entities = [ItemEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}
```

#### 3. Remote Data Source (Network)

**Location:** `data/remote/`

**Structure:**

**API Service:**
```kotlin
interface ApiService {
    @GET("items")
    suspend fun fetchItems(): List<ItemDto>

    @POST("items")
    suspend fun createItem(@Body item: ItemDto): ItemDto

    @GET("items/{id}")
    suspend fun getItemById(@Path("id") id: String): ItemDto
}
```

**DTO (Data Transfer Object):**
```kotlin
@Serializable
data class ItemDto(
    val id: String,
    val name: String,
    val description: String
)

// Extension function to convert DTO to Entity
fun ItemDto.toEntity() = ItemEntity(
    id = id.toIntOrNull() ?: 0,
    name = name,
    description = description
)
```

#### 4. Dependency Injection

**Location:** `data/di/` or `data/<source>/di/`

**Structure:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideFeatureRepository(
        dao: FeatureDao,
        api: ApiService
    ): FeatureRepository = DefaultFeatureRepository(dao, api)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app-database"
        ).build()
    }

    @Provides
    fun provideFeatureDao(database: AppDatabase): FeatureDao {
        return database.featureDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(/* converter */)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
```

---

## Module Organization

### When to Use Multi-Module

**Consider multi-module when:**
- Large team working on different features
- Need to enforce boundaries and reduce coupling
- Want faster build times (parallel builds)
- Planning to share modules across apps
- App has grown beyond 50+ files

### Module Types

#### 1. App Module
- Application class
- Main activity
- Navigation setup
- Depends on all feature modules

#### 2. Feature Modules (`feature-*`)
- Self-contained features
- Contains UI (screens + ViewModels)
- Minimal dependencies
- Pattern: `feature-<feature-name>`

#### 3. Core Modules (`core-*`)
- Shared functionality
- No feature-specific code
- Examples: `core-data`, `core-database`, `core-network`, `core-ui`, `core-domain`

#### 4. Test Module (`core-testing`)
- Shared test utilities
- Fake implementations
- Test runners

### Module Dependencies

```
app
 ├─> feature-feature1
 ├─> feature-feature2
 └─> core-ui

feature-feature1
 ├─> core-data
 ├─> core-domain
 └─> core-ui

core-data
 ├─> core-database
 ├─> core-network
 └─> core-domain

core-database
 └─> core-domain

core-network
 └─> core-domain
```

**Rules:**
- Features should not depend on other features
- Core modules should not depend on feature modules
- Domain module has no dependencies (pure Kotlin)

---

## Best Practices

### General

1. **Follow package-by-feature** for better organization
2. **Use dependency injection** (Hilt) throughout
3. **Write tests** for ViewModels and Repositories
4. **Use sealed interfaces** for state representation
5. **Keep composables small** and focused
6. **Separate stateful and stateless** composables

### UI Layer

1. **StateFlow over LiveData** for Compose
2. **Hoist state** to the appropriate level
3. **Use remember and rememberSaveable** correctly
4. **Create preview composables** for all screens
5. **Extract reusable components** to avoid duplication
6. **Use Modifier parameter** for flexibility

### Data Layer

1. **Repository is the single source of truth**
2. **Expose Flow, not suspend functions** for observable data
3. **Use Room as the source of truth** (offline-first)
4. **Handle errors in Repository** before exposing to UI
5. **Use appropriate dispatchers** for background work
6. **Map data models** appropriately (Entity -> Domain -> UI)

### Testing

1. **Unit test ViewModels** with fake repositories
2. **Use `TestDispatcher`** for coroutine testing
3. **Write UI tests** with Compose testing framework
4. **Create fake implementations** in testing module
5. **Test error cases** and edge cases

---

## Naming Conventions

### Files

- **Screens:** `<Feature>Screen.kt` (e.g., `LoginScreen.kt`)
- **ViewModels:** `<Feature>ViewModel.kt` (e.g., `LoginViewModel.kt`)
- **Repositories:** `<Feature>Repository.kt` (e.g., `UserRepository.kt`)
- **Entities:** `<Feature>Entity.kt` (e.g., `UserEntity.kt`)
- **DAOs:** `<Feature>Dao.kt` (e.g., `UserDao.kt`)
- **DTOs:** `<Feature>Dto.kt` (e.g., `UserDto.kt`)
- **DI Modules:** `<Purpose>Module.kt` (e.g., `DatabaseModule.kt`)

### Packages

- **Feature:** `com.company.app.ui.<feature>`
- **Data:** `com.company.app.data.<source>.<type>`
- **Domain:** `com.company.app.domain.<type>`
- **DI:** `com.company.app.<layer>.di`

### Classes

- **UI State:** `<Feature>UiState`
- **Repository Interface:** `<Feature>Repository`
- **Repository Implementation:** `Default<Feature>Repository`
- **Entity:** `<Feature>Entity`
- **DTO:** `<Feature>Dto`

---

## Example Project Structure

### Small App (Single Module)

```
app/
└── src/main/java/com/example/taskapp/
    ├── TaskApplication.kt
    ├── ui/
    │   ├── MainActivity.kt
    │   ├── Navigation.kt
    │   ├── tasks/
    │   │   ├── TasksScreen.kt
    │   │   └── TasksViewModel.kt
    │   ├── addtask/
    │   │   ├── AddTaskScreen.kt
    │   │   └── AddTaskViewModel.kt
    │   └── theme/
    ├── data/
    │   ├── TaskRepository.kt
    │   ├── local/
    │   │   ├── database/
    │   │   │   ├── AppDatabase.kt
    │   │   │   ├── TaskEntity.kt
    │   │   │   └── TaskDao.kt
    │   │   └── di/
    │   │       └── DatabaseModule.kt
    │   └── di/
    │       └── DataModule.kt
    └── domain/
        └── model/
            └── Task.kt
```

### Large App (Multi-Module)

```
project/
├── app/
├── feature-tasks/
├── feature-addtask/
├── feature-statistics/
├── core-data/
├── core-database/
├── core-domain/
├── core-ui/
└── core-testing/
```

---

## Resources

- [Guide to app architecture](https://developer.android.com/topic/architecture)
- [Now in Android](https://github.com/android/nowinandroid) - Reference architecture
- [Architecture Samples](https://github.com/android/architecture-samples)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)

---

## Summary

This architecture provides:

- Clear separation of concerns
- Testable code
- Scalable structure
- Maintainable codebase
- Reactive UI updates
- Offline-first capability

Start with a single module for small projects, and migrate to multi-module as your project grows.
