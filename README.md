[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-yellow.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Android CI](https://github.com/Polyhoot/android/actions/workflows/android.yml/badge.svg)](https://github.com/Polyhoot/android/actions/workflows/android.yml)

# **Polyhoot! Android client**

<img width="100" height="100" alt="Polyhoot! Logo"
src="https://github.com/Polyhoot/.github/blob/main/logo.jpeg?raw=true" align="right">

### A light-weight Android client for Polyhoot! Written in _Kotlin_ and made with _Material 3_ and _Dynamic Colors support_.

## Download latest release

Polyhoot! is being distributed through **Firebase App Distribution**. You can use this link to get
releases: [**Firebase App Distribution
Invitation**](https://appdistribution.firebase.dev/i/2817cf40c50c4feb)

## Screenshots

<img src="https://i.imgur.com/P0IIMis.png" width="200"> <img src="https://i.imgur.com/81TD4Ce.png" width="200">
<img src="https://i.imgur.com/eJCWVSk.png" width="200">
<img src="https://i.imgur.com/hHzFFGz.png" width="200">

## Build Artifacts

You can download **latest debug package** of the app from _GitHub Action Android CI workflow_ [**here**](https://github.com/Polyhoot/android/actions). Open latest successful action and download zipped APK file from the **Artifacts** section.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

* Java Development Kit 17
* Android SDK Manager

### Cloning

```
git clone https://github.com/polyhoot/android -b master
```

### Using your own server

You can set WebSocket URL to your own server by editing `polyhootWebSocketUrl` field
in `app/gradle.properties`:

```
app/gradle.properties:

/* ... */
# Defines main entry point to polyhoot server infrastructure
# Change this address if you want to run your own server.
polyhootWebSocketUrl=YOUR_URL_HERE
/* ... */
```

### Setting up build environment

Open `local.properties` file and define path to Android SDK using `sdk.dir=` property For example:
`local.properties`

```
# Location of the SDK. This is only used by Gradle.
sdk.dir=<path-to-sdk>
```

### Building

Linux & macOS:

```
./gradlew assembleDebug
```

Windows:

```
.\gradlew.bat assembleDebug
```

### Running on device

Linux & macOS:
```
./gradlew installDebug
```

Windows:
```
.\gradlew.bat installDebug
```

## Built with

* [Android Studio](https://developer.android.com/studio) - Official Android IDE by Google & JetBrains
* [Gradle](https://github.com/gradle/gradle) - Build automation tool
* [Kotlin](https://kotlinlang.org/) - Official programming language for native Android apps
* [Google Material Design 3 Components](https://m3.material.io/) - New default design language for Android 12+
* [OkHttp](https://square.github.io/okhttp) - An HTTP client for handling WebSocket sessions

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.