# Fleur Email App - Project Structure

## Architecture Overview

Fleur follows Clean Architecture principles with a three-layer architecture:

```
takagi.ru.fleur/
├── data/              # Data Layer
│   ├── local/         # Room database, DataStore
│   ├── remote/        # WebDAV client, API
│   └── repository/    # Repository implementations
├── domain/            # Domain Layer
│   ├── model/         # Domain models (Email, Account, etc.)
│   ├── repository/    # Repository interfaces
│   └── usecase/       # Business logic use cases
├── ui/                # Presentation Layer
│   ├── components/    # Reusable UI components
│   ├── navigation/    # Navigation graph
│   ├── screens/       # Screen composables
│   └── theme/         # Material 3 theme
└── di/                # Dependency Injection modules
```

## Technology Stack

### Core
- **Kotlin**: Primary language
- **Jetpack Compose**: Modern UI toolkit
- **Material 3**: Design system

### Architecture
- **Hilt**: Dependency injection
- **Coroutines**: Asynchronous programming
- **Flow**: Reactive data streams

### Data
- **Room**: Local database
- **DataStore**: Preferences storage
- **OkHttp**: HTTP client for WebDAV
- **Retrofit**: REST API client

### UI
- **Coil**: Image loading
- **Navigation Compose**: Navigation

### Background
- **WorkManager**: Background tasks

### Security
- **Security Crypto**: Encrypted storage

## Layer Responsibilities

### Data Layer
- Manages data sources (local and remote)
- Implements repository interfaces
- Handles data caching and synchronization
- WebDAV protocol implementation

### Domain Layer
- Contains business logic
- Defines domain models
- Repository interfaces
- Use cases for specific operations

### UI Layer
- Compose UI components
- ViewModels for state management
- Navigation logic
- Theme and styling

## Dependency Flow

```
UI Layer → Domain Layer → Data Layer
```

- UI depends on Domain
- Domain is independent
- Data implements Domain interfaces

## Getting Started

1. All dependencies are configured in `gradle/libs.versions.toml`
2. Hilt is set up with `FleurApplication` class
3. Package structure follows Clean Architecture
4. Ready for implementation of features
