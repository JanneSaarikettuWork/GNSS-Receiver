# GNSS Receiver Android App 📡📍

A modern, high-precision Android application built with Jetpack Compose that tracks real-time location coordinates, estimates positioning accuracy, inspects multi-constellation GNSS satellite signals (GPS, Galileo, GLONASS, BeiDou), renders skyplots, and provides live NMEA Dilution of Precision (DOP) diagnostics.

---
![](https://github.com/JanneSaarikettuWork/GNSS-Receiver/blob/main/GNSS-main.png "Main view")
![](https://github.com/JanneSaarikettuWork/GNSS-Receiver/blob/main/GNSS-settings.png "Settings view")


## 🌟 Key Features

### 1. 🛰️ Multi-Constellation Source Selection
Select and isolate positioning data from various satellite navigation systems and location providers:
* **Google Location Service (Fused)**: Combines GPS, Wi-Fi, cellular networks, and device sensors for high efficiency and positioning speed.
* **GPS (USA)**: Direct hardware positioning and tracking filtered for US GPS satellites.
* **Galileo (EU)**: Direct hardware positioning and tracking filtered for European Union Galileo satellites.
* **GLONASS (Russia)**: Direct hardware positioning and tracking filtered for Russian GLONASS satellites.
* **BeiDou (China)**: Direct hardware positioning and tracking filtered for Chinese BeiDou satellites.
* **All GNSS Systems**: Raw multi-constellation hardware GNSS receiver combining all visible constellations.

### 2. 📍 Coordinates & Accuracy Metrics
* **Dual Coordinate Formats**: Instant toggle between **Decimal Degrees (DD)** (`60.169854° N, 24.938411° E`) and **Degrees-Minutes-Seconds (DMS)** (`60° 10' 11.5" N, 24° 56' 18.3" E`).
* **Visual Accuracy Badge**: Real-time horizontal error estimate (`± 2.5 m`) categorized into accuracy levels:
  * 🟢 **Excellent** (`< 3 m`)
  * 🟢 **Good** (`3 - 8 m`)
  * 🟠 **Fair** (`8 - 20 m`)
  * 🔴 **Poor** (`> 20 m`)
* **Detailed Position Data**: Altitude (with vertical error), Speed (km/h and m/s), Bearing/Heading (degrees & cardinal direction), Fix Timestamp, and Provider.
* **Action Shortcuts**: 1-tap **Copy Coordinates** to clipboard and **Share Location** via messaging or social apps with direct Google Maps link.

### 3. 🌌 Polar Satellite Skyplot
* Interactive 2D polar radar chart visualizing satellites overhead (Elevation concentric rings at 0°, 30°, 60°, and Azimuth cardinal axes N, E, S, W).
* Color-coded by constellation:
  * 🟦 **GPS**: Blue
  * 🟨 **Galileo**: Amber / Gold
  * 🟥 **GLONASS**: Red
  * 🟩 **BeiDou**: Green
  * 🟪 **SBAS / QZSS / NavIC**: Purple
* Distinct styling for satellites **Used in Fix** (solid circle with SV ID) vs **Visible/Tracked** (hollow outline).

### 4. 📊 Satellite Signal & Frequency Inspector
* Real-time Carrier-to-Noise ratio (**C/N0** in dB-Hz) signal bars.
* Satellite frequency band identification (**L1**, **L5**, **E1**, **E5a**, **B1**, **B2**).
* **Dual-Frequency Detection**: Automatic detection and indicator badge when L1+L5 multi-frequency signals are received.

### 5. 🛠️ NMEA & Dilution of Precision (DOP)
* Real-time Dilution of Precision metrics:
  * **HDOP** (Horizontal Dilution of Precision)
  * **VDOP** (Vertical Dilution of Precision)
  * **PDOP** (Position Dilution of Precision)
* DOP Quality rating (`Ideal`, `Excellent`, `Good`, `Moderate`, `Fair`, `Poor`).
* **Live NMEA Terminal**: Expandable viewer displaying live raw incoming NMEA sentences (`$GPGGA`, `$GNGSA`, `$GPGSV`, etc.).

### 6. ⚙️ Receiver Settings
* Select which coordinate sources are active and enabled in the main selector.
* Adjustable **Location Update Interval** (Fast 0.5s, Normal 1.0s, Battery Saver 3.0s).
* **Keep Screen Awake** toggle for uninterrupted outdoor satellite observations.
* **Default DMS Format** setting.
* Full integration with Android system back-gesture / back button navigation.

---

## 🏗️ Architecture & Tech Stack

* **Language**: Kotlin 100%
* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
* **Architecture**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow with Kotlin `StateFlow` & `Coroutines`
* **Location & Hardware APIs**:
  * `LocationManager` & `GnssStatus.Callback`
  * `OnNmeaMessageListener`
  * Google Play Services `FusedLocationProviderClient`
* **Persistence**: `SharedPreferences` via `SettingsRepository`
* **Build System**: Gradle Version Catalogs (`libs.versions.toml`)

---

## 📋 Prerequisites & Permissions

* **Min SDK**: 36
* **Target SDK**: 37
* **Required Permissions**:
  * `android.permission.ACCESS_FINE_LOCATION`
  * `android.permission.ACCESS_COARSE_LOCATION`

---

## 🚀 Building & Running

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/JanneSaarikettuWork/GNSSreceiver.git
   cd GNSSreceiver
   ```

2. **Open in Android Studio**:
   Open the project folder in Android Studio Ladybug or later.

3. **Build the Project**:
   Run via Gradle:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

5. **Deploy to Device**:
   Connect an Android device with USB Debugging enabled and run from Android Studio (`Shift` + `F10`) or via ADB:
   ```bash
   ./gradlew installDebug
   ```

---

## 📄 License

```text
Copyright 2026

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
