# Project Directory Structure - Food-Track-Share

This document provides a summary of the `DoAnCoSo3` project structure.

## Tree Overview

```text
DoAnCoSo3/
├── app/                        # Main application module
│   ├── schemas/                # Room database exported schemas
│   └── src/
│       ├── main/
│       │   ├── java/com/example/doancoso3/
│       │   │   ├── data/           # Data layer (Local, Remote, Repositories, Models)
│       │   │   │   ├── local/      # Room Database, DAOs
│       │   │   │   ├── model/      # Entities and Data classes
│       │   │   │   ├── remote/     # Firebase interactions
│       │   │   │   └── repository/ # Repository implementations
│       │   │   ├── di/             # Hilt Dependency Injection modules
│       │   │   ├── ui/             # UI layer (Jetpack Compose screens, ViewModels)
│       │   │   │   ├── auth/       # Authentication flow
│       │   │   │   ├── fooditem/   # Food inventory management
│       │   │   │   ├── group/      # Family group management
│       │   │   │   └── ...         # Other feature-based packages
│       │   │   └── utils/          # Helpers and Validators
│       │   ├── res/                # Android resources (drawables, layouts, values)
│       │   └── AndroidManifest.xml # App manifest
│       ├── test/               # Unit tests (Kotest, MockK)
│       └── androidTest/        # Instrumented tests (Espresso, Compose UI tests)
├── docs/                       # Documentation (Diagrams, ERD)
├── build.gradle.kts            # Project-level build configuration
└── README.md                   # Basic project info
```

## Key Directories

### `app/src/main/java/.../data/`
- **local/**: Manages local persistence using Room.
- **remote/**: Handles backend communication with Firebase.
- **repository/**: The "Single Source of Truth" coordinating between local and remote data.
- **model/**: Defines the domain and data entities used throughout the app.

### `app/src/main/java/.../ui/`
Organized by feature. Each package typically contains:
- **Compose Screens**: Declarative UI components.
- **ViewModels**: Logic and state management using Hilt and StateFlow.

### `app/src/main/java/.../di/`
Contains Hilt modules for providing dependencies like the database, repositories, and Firebase instances.

### `docs/`
Contains technical documentation, including ER diagrams (`erd.md`) and activity diagrams (`.puml`).
