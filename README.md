<div align="center">

# 🌐 Wi-Fi Router Application

**High-Performance Native Android Management Dashboard & Telemetry Companion for Wi-Fi Routers**

[![Release](https://img.shields.io/github/v/release/smartworldarafath/WiFi-Router-Application?style=for-the-badge&color=005BAC&logo=github)](https://github.com/smartworldarafath/WiFi-Router-Application/releases)
[![API](https://img.shields.io/badge/API-24%2B%20(Android%207.0%2B)-3DDC84?style=for-the-badge&logo=android)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024-4285F4?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/github/license/smartworldarafath/WiFi-Router-Application?style=for-the-badge&color=blue)](LICENSE)

<br/>

<p align="center">
  <img src="docs/art/icon-1-default.webp" width="105" alt="Default Icon" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="docs/art/icon-2-cyber.png" width="105" alt="Cyber Icon" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="docs/art/icon-3-neon.png" width="105" alt="Neon Icon" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="docs/art/icon-4-midnight.png" width="105" alt="Midnight Icon" />
</p>

*Custom launcher themes: Netis Electric Wave, Cyber Chassis, Neon Pulse, and Midnight Stealth.*

</div>

---

## 📖 Overview

**Wi-Fi Router Application** is a native Android utility engineered with **Jetpack Compose** and **Material 3**. Designed specifically to modernize, enhance, and streamline router administration (tailored for Netis routers such as WF2409E, WF2419, WF2780, as well as auto-detecting TP-Link, Tenda, Cudy, Mercusys, ASUS, D-Link, and more), this application replaces clunky legacy browser interfaces with a fluid, ultra-responsive dashboard.

It delivers real-time telemetry, live WAN traffic meters, AI-assisted network troubleshooting via Gemini, connected device controls, QoS throttling, and custom hardware performance tuning right from your pocket.

---

## ✨ Features

### 🚀 Gateway Detection & Router Web Portal
- **Zero-Config Gateway Autodetect**: Scans network interfaces, extracts default router gateway IP, and determines brand profile (`TP-Link`, `Tenda`, `Netis`, `Mercusys`, `Cudy`, `ASUS`, `MikroTik`, `FRITZ!Box`, etc.).
- **Smart Integrated Browser**: Embedded hardware-accelerated WebView engine with automated cookie synchronization, pull-to-refresh, zoom controls, and quick gateway shortcuts (`192.168.1.1`, `192.168.0.1`, `192.168.10.1`, custom).

### 📊 Real-Time Native Dashboard & Diagnostics
- **Live Traffic Telemetry**: Analog speedometer gauge and smooth water wave graphs tracking WAN upload/download transfer rates.
- **Wireless Management**: Toggle 2.4GHz & 5GHz bands, inspect SSID details, security modes (WPA/WPA2/WPA3-PSK), channel status, and live client counters.
- **Client Table & Control**: Complete connected device list showing IP, MAC address, hostnames, and quick action buttons for instant blocklisting and QoS speed capping.
- **Network Diagnostic Suite**: Built-in Ping test (min/avg/max latency & jitter), Traceroute, and remote router reboot countdown safeguards.


### 🤖 Netis AI Assistant (Gemini Powered)
- Integrated Google Gemini AI diagnostics engine that analyzes real-time signal conditions, channel overlap, DNS latency, and suggests actionable network performance optimizations.

### ⚡ Performance Profiles & Display Refresh Sync
- **Performance Modes**: Choose between *Economy*, *Balanced*, *Performance*, and *Ultra* power/refresh tuning profiles.
- **Hardware Refresh Rate Switching**: Support for 60Hz, 90Hz, and 120Hz display modes with live hardware frame pacing and live CPU/GPU/memory tracking.
- **Dynamic App Launcher Icons**: Pick from 5 crafted home screen launcher styles on the fly via `IconSwitchManager` (Default, Cyber, Neon, Midnight, and Solar).

### 🔄 In-App Updates & Community Feedback
- **Automated GitHub Release Tracking**: Checks directly against [GitHub Releases](https://github.com/smartworldarafath/WiFi-Router-Application/releases) for new builds, displaying full release notes, build hashes, and one-tap APK download triggers.
- **Direct Feedback & Telemetry**: Integrated bug reporting and feature request screen with auto-attached device diagnostics (Android version, device model, display mode).

---

## 🛠️ Architecture & Tech Stack

```
com.example
├── MainActivity.kt               # Edge-to-edge entry point, drawer coordinator & navigation
├── ai/
│   └── NetisAiAssistant.kt       # Gemini API client & contextual WiFi diagnostic engine
├── data/
│   ├── AppPreferences.kt         # DataStore preferences (themes, refresh modes, tokens)
│   ├── RouterDetectionManager.kt # Multi-brand Wi-Fi gateway autodetection engine
│   ├── RouterModels.kt           # Domain models (Status, Device, Rule, Latency)
│   └── RouterRepository.kt       # Coroutine flow state manager & live mock/real poller
├── feedback/
│   └── FeedbackService.kt        # Multi-channel feedback dispatcher with auto-diagnostics
├── performance/
│   ├── IconSwitchManager.kt      # Dynamic Activity-Alias switcher for custom icons
│   ├── PerformanceMode.kt        # Performance profile enum & refresh definitions
│   ├── PerformanceMonitor.kt     # Real-time CPU, RAM, and FPS load sampler
│   └── RefreshRateManager.kt     # DisplayMode query and refresh rate override
├── ui/
│   ├── components/               # Custom Speedometers, Analog Meters, Gauges & Drawers
│   ├── screens/                  # DashboardScreen, RouterWebScreen, Settings, Updates, Feedback
│   └── theme/                    # Material 3 color palettes, typography, and dark mode
└── update/
    └── GitHubReleaseService.kt   # GitHub REST API release fetcher and fallback manager
```

- **Language**: [Kotlin](https://kotlinlang.org/) (Coroutines + Flow)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
- **Network**: [OkHttp 4](https://square.github.io/okhttp/) & [Moshi](https://github.com/square/moshi)
- **Storage**: [Android Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (Preferences)
- **AI Engine**: Google GenAI Client SDK / Firebase AI
- **Image Loading**: [Coil Compose](https://coil-kt.github.io/coil/)

---

## 📥 Download & Installation

Pre-compiled and signed APK binaries are available directly from the repository's GitHub Releases page:

| Version | Build | Release Type | Direct Download |
| :--- | :---: | :---: | :--- |
| **v1.0.2** | Build 3 | Latest Stable | [Download APK](https://github.com/smartworldarafath/WiFi-Router-Application/releases/download/v1.0.2/WiFi.Router.Application.v1.0.2.apk) |
| **v1.0.0** | Build 1 | Initial Release | [Download APK](https://github.com/smartworldarafath/WiFi-Router-Application/releases/download/v1.0.0/WiFi.Router.Application.v1.0.0.apk) |

> **Note**: Android will prompt to "Allow installation from this source" when installing via third-party downloads or browser.

---

## 💻 Building from Source

### Prerequisites
- [Android Studio Ladybug (2024.2+)](https://developer.android.com/studio) or newer
- JDK 17 or JDK 21 installed and configured in your environment
- Android SDK with Platform 36 (Android 15+) installed

### Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/smartworldarafath/WiFi-Router-Application.git
   cd WiFi-Router-Application
   ```

2. **Configure Environment Secrets (Optional for AI features)**:
   Copy `.env.example` to `.env` in the project root:
   ```bash
   cp .env.example .env
   ```
   Add your Google Gemini API key to `.env`:
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   ```

3. **Build the Debug APK**:
   - On Linux/macOS:
     ```bash
     ./gradlew assembleDebug
     ```
   - On Windows:
     ```powershell
     .\gradlew.bat assembleDebug
     ```

4. **Install onto a connected Android device**:
   ```bash
   ./gradlew installDebug
   ```

---

## 🔒 Security & Privacy

- **Local Gateway Communication**: All router administrative interactions happen strictly within your local area network (LAN) over your Wi-Fi interface. Credentials are never harvested or transmitted to external third-party tracking servers.
- **Safe Secrets Handling**: API credentials and secrets are managed via `secrets-gradle-plugin` using local `.env` storage, keeping repository code clean of hardcoded private keys.

---

## 🤝 Contributing

Contributions, bug reports, and suggestions are welcome!

1. Fork the Project (`https://github.com/smartworldarafath/WiFi-Router-Application/fork`)
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

Distributed under the MIT License. See [`LICENSE`](LICENSE) for more information.

---

<div align="center">
  Crafted with ❤️ by <a href="https://github.com/smartworldarafath">smartworldarafath</a>
</div>
