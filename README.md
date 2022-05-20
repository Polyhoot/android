[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-yellow.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Android CI](https://github.com/Polyhoot/android/actions/workflows/android.yml/badge.svg)](https://github.com/Polyhoot/android/actions/workflows/android.yml)
# **Polyhoot Android client**

_A light-weight Android client for Polyhoot game. Written in **Kotlin** and made with **Material Design 3** and Android 12 **Dynamic Colors support**._

## _Screenshots_

<img src="https://i.imgur.com/P0IIMis.png" width="200"> <img src="https://i.imgur.com/81TD4Ce.png" width="200">
<img src="https://i.imgur.com/eJCWVSk.png" width="200">
<img src="https://i.imgur.com/hHzFFGz.png" width="200">

## _Build Artifacts_

You can download **latest debug package** of the app from _GitHub Action Android CI workflow_ [**here**](https://github.com/Polyhoot/android/actions). Open latest successful action and download zipped APK file from the **Artifacts** section.

## _Getting Started_

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### _Prerequisites_

* Java Development Kit 17

### _Cloning_

```
git clone https://github.com/polyhoot/android -b master
```

### _Building_

Linux & macOS:
```
./gradlew build assembleDebug
```

Windows:
```
.\gradlew.bat build assembleDebug
```

### _Running on device_

Linux & macOS:
```
./gradlew build installDebug
```

Windows:
```
.\gradlew.bat build installDebug
```

## _Built with_

* [Android Studio](https://developer.android.com/studio) - Official Android IDE by Google & JetBrains
* [Gradle](https://github.com/gradle/gradle) - Build automation tool
* [Kotlin](https://kotlinlang.org/) - Official programming language for native Android apps
* [Google Material Design 3 Components](https://m3.material.io/) - New default design language for Android 12+
* [OkHttp](https://square.github.io/okhttp) - An HTTP client for handling WebSocket sessions

## _License_

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
