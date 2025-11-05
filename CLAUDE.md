# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

RikkaHub is a native Android LLM chat client built with Jetpack Compose, Kotlin, and Material Design 3. It supports switching between different AI providers for conversations and features multimodal input, MCP support, and advanced capabilities like memory tables and world books.

## Architecture Overview

### Module Structure

- **app** (`app/`): Main application module with UI, ViewModels, core business logic, and Firebase integration
- **ai** (`ai/`): AI SDK abstraction layer for different providers (OpenAI, Google, Anthropic, Vertex)
- **highlight** (`highlight/`): Code syntax highlighting implementation
- **search** (`search/`): Search functionality SDK (Exa, Tavily, Zhipu)
- **tts** (`tts/`): Text-to-speech implementation for different providers
- **document** (`document/`): Document processing capabilities (PDF, DOCX)
- **common** (`common/`): Common utilities and extensions shared across modules

### Architecture Patterns

The project follows **MVVM + Repository + Clean Architecture** pattern:

- **Presentation Layer (UI)**: Jetpack Compose UI with ViewModels
- **Domain Layer**: Use cases and business logic (mainly in `app/data/` and `app/service/`)
- **Data Layer**: Repository pattern with Room database and DataStore preferences

### Dependency Injection

Uses **Koin** for dependency injection with modules defined in:
- `app/src/main/java/me/rerere/rikkahub/di/AppModule.kt` - App-level dependencies (Firebase, services, utils)
- `app/src/main/java/me/rerere/rikkahub/di/RepositoryModule.kt` - Repository layer
- `app/src/main/java/me/rerere/rikkahub/di/ViewModelModule.kt` - ViewModels

### Core Components

**Database (Room)**:
- Main database: `AppDatabase.kt` (version 12)
- Entities: `ConversationEntity`, `MemoryEntity`, `WorldBookEntry`, `MemoryTable`, `MemoryTableRow`
- DAOs: `ConversationDAO`, `MemoryDAO`, `WorldBookDAO`, `MemoryTableDAO`
- Migration from v6 to v7 handles message format change (List<UIMessage> → List<MessageNode>)

**AI Integration**:
- Provider abstraction: `ai/src/main/java/me/rerere/ai/provider/Provider.kt`
- Implementations: `OpenAIProvider.kt`, `GoogleProvider.kt`, `ClaudeProvider.kt`
- Chat service: `app/src/main/java/me/rerere/rikkahub/service/ChatService.kt`
- Generation handler: `app/src/main/java/me/rerere/rikkahub/data/ai/GenerationHandler.kt`

**UI Structure**:
- `app/src/main/java/me/rerere/rikkahub/ui/pages/`: Screen implementations
- `app/src/main/java/me/rerere/rikkahub/ui/components/`: Reusable UI components
- Navigation using Navigation Compose

**MCP Support**:
- `app/src/main/java/me/rerere/rikkahub/data/ai/mcp/McpManager.kt`
- Transport implementations for SSE and HTTP

## Common Development Commands

### Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build both APK and AAB
./gradlew buildAll

# Build for specific ABI (arm64-v8a, x86_64)
./gradlew assembleRelease

# Clean build
./gradlew clean
```

### Testing

```bash
# Run all unit tests
./gradlew test

# Run tests for specific module
./gradlew :app:test
./gradlew :ai:test

# Run unit tests with coverage
./gradlew testDebugUnitTest

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew :app:testDebugUnitTest --tests="me.rerere.rikkahub.MemoryTableRepositoryTest"

# Run specific test method
./gradlew :app:testDebugUnitTest --tests="me.rerere.rikkahub.MemoryTableRepositoryTest.test basic table structure"
```

### Lint and Code Quality

```bash
# Run Android lint
./gradlew lint

# Run Compose lint checks
./gradlew :app:lintDebug

# KSP generated code verification
./gradlew :app:kspDebugKotlin
```

### Development Tasks

```bash
# Sync project with Gradle files
./gradlew --refresh-dependencies

# Run baseline profile generation
./gradlew :app:generateBaselineProfile

# Build with profiling enabled
./gradlew :app:assembleBaseline
```

### Module-Specific Commands

```bash
# Build specific module as AAR
./gradlew :ai:assemble

# Build search module
./gradlew :search:assemble

# Build tts module
./gradlew :tts:assemble
```

### Dependency Management

```bash
# Clean build cache
./gradlew cleanBuildCache

# Build without tests
./gradlew assembleRelease -x test
```

## Key Technologies

- **Jetpack Compose**: UI framework (Material 3)
- **Kotlin**: Development language
- **Koin**: Dependency injection
- **Room**: Database ORM with migrations
- **DataStore**: Preferences storage
- **OkHttp**: HTTP client with SSE support
- **Navigation Compose**: App navigation
- **Kotlinx Serialization**: JSON handling
- **Firebase**: RemoteConfig, Analytics, Crashlytics
- **Retrofit**: REST client
- **Coil**: Image loading
- **Paging 3**: Data pagination

## SDK Requirements

- **Compile SDK**: 36
- **Target SDK**: 36
- **Min SDK**: 26
- **JVM Target**: 17 (app), 11 (library modules)
- **Supported ABIs**: arm64-v8a, x86_64

## Build Configuration

### Version Catalog

Located in `gradle/libs.versions.toml`, manages all dependency versions centrally.

### Signing Configuration

Release builds require signing keys configured in `local.properties`:
```properties
storeFile=path/to/app.key
storePassword=xxxx
keyAlias=xxxx
keyPassword=xxxx
```

### Firebase Configuration

Requires `google-services.json` in the `app/` folder for Firebase features (RemoteConfig, Analytics, Crashlytics).

### CI/CD

GitHub Actions workflow at `.github/workflows/release.yml`:
- Automated release builds
- Builds APK using GitHub secrets for signing keys and Firebase config
- Caches Gradle dependencies
- Uploads artifacts

## Important File Paths

### App Module
- `app/src/main/java/me/rerere/rikkahub/RikkaHubApp.kt`: Application class with Koin setup and Firebase initialization
- `app/src/main/java/me/rerere/rikkahub/data/db/AppDatabase.kt`: Room database with migrations
- `app/src/main/java/me/rerere/rikkahub/data/datastore/PreferencesStore.kt`: DataStore preferences
- `app/src/main/java/me/rerere/rikkahub/service/ChatService.kt`: Core chat service
- `app/build.gradle.kts`: App module build configuration

### AI Module
- `ai/src/main/java/me/rerere/ai/provider/Provider.kt`: Provider abstraction
- `ai/src/main/java/me/rerere/ai/provider/providers/OpenAIProvider.kt`: OpenAI implementation
- `ai/src/main/java/me/rerere/ai/provider/ProviderManager.kt`: Provider management
- `ai/build.gradle.kts`: AI module build configuration

### Database Schema
- `app/schemas/`: Room database schema files (auto-generated)

## Development Notes

### Material Design 3

UI components should follow Material You design principles. Use:
- `Material3` components from Compose
- `LocalToaster.current` for toast messages
- `Lucide.XXX` icons from `com.composables.icons.lucide`

### Message Format

Messages have evolved from `List<UIMessage>` to `List<MessageNode>` for better message branching support (migration in AppDatabase v7).

### Testing Structure

- Unit tests in `src/test/`: Fast, no Android framework dependencies
- Instrumented tests in `src/androidTest/`: Require Android device/emulator
- Test examples:
  - `app/src/test/java/me/rerere/rikkahub/MemoryTableRepositoryTest.kt`: Data model tests
  - `ai/src/test/java/me/rerere/ai/ModelRegistryTest.kt`: AI module tests

### Room Migrations

Database version 12 includes multiple migrations:
- Auto-migrations for versions 1-6, 7-9 (with spec), 9-12
- Custom migration `Migration_6_7` for message format change
- See `AppDatabase.kt:85-176` for migration implementation

### Kotlin Target Compatibility

- App module uses JVM 17
- Library modules (ai, search, tts, etc.) use JVM 11
- This is configured in respective `build.gradle.kts` files

### Compose Compiler

Enabled experimental APIs in `app/build.gradle.kts:122-133`:
- Material 3 experimental APIs
- Animation APIs
- Foundation APIs
- UUID, Time, and Coroutines experimental APIs

## Contribution Guidelines

**Rejected PR Types** (per README):
1. Translation changes (new languages or updating translations)
2. New features (project is opinionated)
3. Large-scale refactoring generated by AI

**Required Files**:
- `google-services.json` must be present in `app/` folder for builds
- Signing keys in `local.properties` for release builds

## Troubleshooting

### Build Failures

If build fails with KSP/Room issues:
```bash
./gradlew clean
rm -rf app/build
rm -rf app/schemas
./gradlew :app:kspDebugKotlin
```

### Test Failures

For Room-related test failures, ensure test dependencies are correct:
```bash
./gradlew :app:testDebugUnitTest --info
```

### Gradle Cache Issues

Clear Gradle cache if builds behave unexpectedly:
```bash
./gradlew --stop
rm -rf ~/.gradle/caches
./gradlew --refresh-dependencies
```
- rikkahub这一项目开发环境为Windows