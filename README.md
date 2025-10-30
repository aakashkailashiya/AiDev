# Ai Dev

Ai Dev is an advanced Android application designed to provide device diagnostics, hardware information, developer tools, and productivity utilities in a modern, Material 3 Compose UI. It is built using Kotlin and leverages Jetpack Compose for a rich, interactive experience.

## Features
- **Device & Hardware Info**: View live CPU, RAM, battery, and storage usage. Explore detailed hardware specs, build info, and system properties.
- **Sensors Explorer**: Discover and inspect all available device sensors, grouped by type (motion, position, environment).
- **Connectivity Analyzer**: Inspect Wi-Fi and cellular details, including signal strength, network type, and bandwidth.
- **App Analyzer**: List installed apps, view permissions, and basic manifest info.
- **Developer Tools**: Logcat viewer, media codecs explorer, and quick access to developer settings.
- **Productivity Tools**: Pomodoro timer, power button logging, and more.

## Getting Started

### Prerequisites
- Android Studio (Arctic Fox or newer recommended)
- JDK 11 or newer
- Android SDK (API 24+)

### Build & Run
1. Clone this repository:
   ```sh
   git clone https://github.com/aakashkailashiya/AiDev.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and build the project.
4. Run on an emulator or physical device (API 24+).

### Gradle
- Uses Kotlin DSL (`build.gradle.kts`)
- Compose, Material3, and Coil dependencies managed via `libs.versions.toml`

## Usage
- Launch the app to access the main dashboard.
- Navigate through categories for hardware info, sensors, connectivity, developer tools, and productivity features.
- Some features require permissions (e.g., storage, phone state, network access).

## Contribution
Contributions are welcome! Please fork the repo, create a feature branch, and submit a pull request. For major changes, open an issue first to discuss your ideas.

## License
This project is licensed under the Apache License 2.0. See the `LICENSE` file for details.

---

Maintained by [aakashkailashiya](https://github.com/aakashkailashiya)
