# Ai Dev
<div align="center">
  <img 
    src="https://github.com/user-attachments/assets/111be0f4-5406-4866-9d0e-9a47b4cd54a5" 
    alt="ai dev" 
    width="1024" 
    height="1024" 
  />
</div>


[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-orange.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API%2024%2B-green.svg)](https://developer.android.com/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.0-blue.svg)](https://developer.android.com/jetpack/compose)

**Ai Dev** is a powerful, open-source Android app that empowers developers and tech enthusiasts with comprehensive device diagnostics, hardware insights, and productivity tools—all wrapped in a sleek, modern Material 3 UI built with Jetpack Compose and Kotlin. Whether you're debugging hardware issues, exploring sensors, or boosting your workflow, Ai Dev puts advanced diagnostics at your fingertips.

<div align="center">

  <!-- Dashboard Screenshots -->
  <img src="https://github.com/user-attachments/assets/7310a7f1-75e3-4186-bd56-eed426ef36f9" width="250" alt="Dev Screenshot 6">
  <img src="https://github.com/user-attachments/assets/7fe9bf35-6a2c-497c-9b60-baac0a7daff3" width="250" alt="Dev Screenshot 7">
  <img src="https://github.com/user-attachments/assets/2d399162-b3fb-45b8-9df1-2c2e8acf1b82" width="250" alt="Dev Screenshot 5">
  <img src="https://github.com/user-attachments/assets/b7f4b8c1-8635-4461-b8bb-a98617aac9b7" width="250" alt="Dev Screenshot 2">
    <img src="https://github.com/user-attachments/assets/4339a4eb-c24e-4bda-b758-635152feb8a1" width="250" alt="Dev Screenshot 1">

  <img src="https://github.com/user-attachments/assets/785f2633-c8d2-43e1-a087-bb4165c26d67" width="250" alt="Dev Screenshot 4">
   <img src="https://github.com/user-attachments/assets/439cc71a-eecd-4514-8b87-dbe8e1f7ba13" width="250" alt="Dev Screenshot 3">

  


  <p><em>Main Dashboard: Quick access to all features</em></p>
</div>


## ✨ Key Features

- **📊 Device & Hardware Intelligence**
  - Real-time monitoring of CPU, RAM, battery, and storage metrics.
  - In-depth specs: Processor details, GPU info, display properties, and build fingerprints.
  - System properties explorer for advanced tweaking and debugging.

- **🔍 Sensors Explorer**
  - Full catalog of device sensors (accelerometer, gyroscope, light, proximity, etc.).
  - Grouped by category: Motion, Position, Environmental, and more.
  - Live data visualization and event logging for precise analysis.

- **🌐 Connectivity Analyzer**
  - Wi-Fi diagnostics: SSID, BSSID, signal strength, channel, and speed tests.
  - Cellular insights: Network type (5G/4G), carrier details, and data usage.
  - Bluetooth and NFC scanning for comprehensive network health checks.

- **📱 App Analyzer**
  - Complete list of installed apps with version, size, and last update info.
  - Permission breakdowns and manifest parsing for security audits.
  - Exportable reports for compliance or debugging.

- **🔧 Developer Toolkit**
  - Built-in Logcat viewer with filters and real-time streaming.
  - Media codecs inspector for video/audio compatibility testing.
  - One-tap access to ADB, developer options, and USB debugging.

- **⚡ Productivity Boosters**
  - Customizable Pomodoro timer with notifications and session tracking.
  - Power button event logger for gesture customization.
  - Quick notes and clipboard history for on-the-go efficiency.

## 🚀 Quick Start

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1) or later.
- **JDK**: 17 or higher (for optimal Compose support).
- **Android SDK**: Target API 34+, minimum API 24.
- **Emulator/Device**: Pixel 6+ emulator or physical device with developer options enabled.

### Installation & Setup
1. **Clone the Repo**:
   ```bash
   git clone https://github.com/aakashkailashiya/AiDev.git
   cd AiDev
   ```

2. **Open in Android Studio**:
   - Import the project and let Gradle sync (it uses Kotlin DSL for cleaner builds).
   - Review `libs.versions.toml` for version management of Compose, Hilt, and other libs.

3. **Build & Run**:
   ```bash
   ./gradlew assembleDebug  # For debug APK
   ```
   - Connect your device or start an emulator (API 24+).
   - Hit **Run** (Shift + F10) and grant permissions as prompted (e.g., for sensors, location, and storage).

### APK Download
For quick testing without building:
- Download the latest release from [GitHub Releases](https://github.com/aakashkailashiya/AiDev/releases).
- Install via `adb install app-debug.apk` or sideload directly.

## 📖 Usage Guide

1. **Launch & Navigate**:
   - Open the app to land on the intuitive dashboard.
   - Use the bottom navigation bar for core sections: **Info**, **Sensors**, **Network**, **Apps**, **Tools**, **Productivity**.

2. **Permissions**:
   - The app requests runtime permissions for accurate data (e.g., `READ_PHONE_STATE` for network, `BODY_SENSORS` for health metrics).
   - Check **Settings > Permissions** in-app for granular control.

3. **Pro Tips**:
   - Enable dark mode via system settings for the best Material 3 experience.
   - Use the search bar in explorers to filter sensors or apps quickly.
   - Export logs/reports as JSON/CSV for external analysis.

<div align="center">
  <img src="screenshots/sensors.png" width="300" alt="Sensors Explorer Screenshot"> 
  <img src="screenshots/connectivity.png" width="300" alt="Connectivity Analyzer Screenshot">
  <p><em>Left: Sensors in action | Right: Network diagnostics</em></p>
</div>

## 🛠️ Tech Stack
- **Language**: Kotlin 1.9+
- **UI**: Jetpack Compose (Beta) + Material 3
- **Architecture**: MVVM with Hilt for DI, Coroutines/Flow for async ops
- **Libs**: Coil (image loading), Accompanist (Compose extras), Room (local DB for logs)
- **Testing**: JUnit 5, Espresso, Compose UI Testing
- **Build**: Gradle 8.0+ with Kotlin DSL

## 🤝 Contributing

We love contributions! Help make Ai Dev even better by adding features, fixing bugs, or improving docs.

1. **Fork** the repo and create a feature branch: `git checkout -b feature/amazing-new-tool`.
2. **Commit** your changes: `git commit -m "Add: New sensor visualization"`.
3. **Push** to the branch: `git push origin feature/amazing-new-tool`.
4. **Pull Request**: Open a PR against `main`. Include a description of your changes and why they matter.

**Guidelines**:
- Follow [Google's Android Style Guide](https://developer.android.com/kotlin/style-guide).
- Add tests for new features.
- For big ideas, [open an issue](https://github.com/aakashkailashiya/AiDev/issues/new) first to align.

## 📄 License

This project is licensed under the [Apache License 2.0](LICENSE) – see the LICENSE file for details.

## 🙌 Acknowledgments

- Built with ❤️ by [Aakash Kailashiya](https://github.com/aakashkailashiya).
- Inspired by tools like CPU-Z, Sensor Kinetics, and DevTools.
- Thanks to the Jetpack Compose and Kotlin communities!

---

⭐ **Star the repo** if Ai Dev saves you time! Have questions? [Open an issue](https://github.com/aakashkailashiya/AiDev/issues) or join the discussion. Feedback welcome!
