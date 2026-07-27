# QR Code Scanner & Barcode Scanner – Fast QR Reader
### Package: `com.nexuzstudios.qrcodescanner_quickscan`
### By: NexuzStudios

---

## 🚀 Project Overview

A production-ready Android app for QR code scanning, barcode scanning, QR code generation, and barcode generation — built with modern Jetpack Compose and MVVM architecture, optimized for Play Store ranking.

---

## 🏗 Architecture

```
Clean Architecture + MVVM
├── data/
│   ├── db/          → Room entities, DAOs, Database
│   └── repository/  → QRRepository (single source of truth)
├── domain/
│   ├── model/       → ScanResult, GeneratedQR, WifiCredentials, ContactInfo
│   └── usecase/     → (extensible use cases)
├── ui/
│   ├── screens/
│   │   ├── scan/    → ScanScreen + CameraX + ML Kit
│   │   ├── create/  → CreateScreen + ZXing generator
│   │   ├── history/ → HistoryScreen + Room data
│   │   └── settings/→ SettingsScreen + DataStore prefs
│   ├── components/  → BannerAdView, shared components
│   └── theme/       → Material 3 colors, typography
├── viewmodel/       → ScanVM, CreateVM, HistoryVM, SettingsVM
├── di/              → Hilt DatabaseModule
├── ads/             → AdManager (AdMob)
└── utils/           → QRGenerator, ContentDetector, HapticUtil, etc.
```

---

## 📦 Tech Stack

| Technology | Usage |
|---|---|
| Kotlin | Primary language |
| Jetpack Compose + Material 3 | UI framework |
| CameraX | Camera lifecycle management |
| ML Kit Barcode Scanning | QR/Barcode detection |
| ZXing | QR/Barcode generation |
| Hilt | Dependency injection |
| Room | Local scan/generated history |
| DataStore Preferences | User settings |
| Kotlin Coroutines + Flow | Async/reactive data |
| AdMob | Monetization |
| Google Play Billing | Pro in-app purchase |

---

## ⚡ Setup Instructions

### 1. Clone & Open
```bash
git clone <repo>
# Open in Android Studio Iguana or later
```

### 2. Replace AdMob IDs
In `app/build.gradle.kts`:
```kotlin
manifestPlaceholders["admobAppId"] = "YOUR_REAL_ADMOB_APP_ID"
```

In `AdManager.kt`:
```kotlin
const val INTERSTITIAL_ID = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
const val BANNER_ID = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
const val APP_OPEN_ID = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
```

### 3. Replace Package Name (if needed)
Use Android Studio's "Rename Package" refactor tool.

### 4. Add Signing Config
In `app/build.gradle.kts` release block:
```kotlin
signingConfig = signingConfigs.getByName("release")
```

### 5. Build
```bash
./gradlew assembleRelease
```

---

## 🎨 Design System

| Token | Value | Usage |
|---|---|---|
| `DarkBackground` | `#121212` | Main background |
| `SurfaceDark` | `#1E1E1E` | Cards, bottom nav |
| `NeonGreen` | `#00E676` | Primary actions, accents |
| `NeonBlue` | `#1E88E5` | Secondary actions |
| `ErrorRed` | `#CF6679` | Delete, errors |

---

## 💰 Monetization

| Ad Type | Trigger | Unit |
|---|---|---|
| App Open Ad | App foreground | `APP_OPEN_ID` |
| Interstitial | Every 3 scans | `INTERSTITIAL_ID` |
| Banner | History screen | `BANNER_ID` |
| Pro (Remove Ads) | One-time purchase | `$1.99` |

---

## 📱 Play Store ASO

See `PLAY_STORE_ASO.md` for:
- App title (optimized)
- Short description
- Long description (keyword-rich)
- Keyword strategy

---

## 🔑 Permissions Required

| Permission | Reason |
|---|---|
| `CAMERA` | QR/Barcode scanning |
| `VIBRATE` | Haptic feedback |
| `INTERNET` | AdMob ads |
| `READ_MEDIA_IMAGES` | Scan from gallery (API 33+) |
| `WRITE_EXTERNAL_STORAGE` | Save QR images (API < 29) |

---

## 📋 Features Checklist

### Scanner
- [x] QR Code scanning (ML Kit)
- [x] Barcode scanning (all formats)
- [x] Flashlight toggle
- [x] Haptic + beep feedback
- [x] Copy result
- [x] Share result
- [x] Open URLs
- [x] Animated scanner overlay

### Generator
- [x] Text QR
- [x] URL QR
- [x] WiFi QR
- [x] Contact/vCard QR
- [x] Email QR
- [x] Phone QR
- [x] SMS QR
- [x] Code128 barcode
- [x] EAN-13 barcode
- [x] EAN-8 barcode
- [x] Save to gallery
- [x] Share generated QR

### History
- [x] Scan history (Room)
- [x] Generated QR history (Room)
- [x] Favorites system
- [x] Copy/Share/Delete
- [x] Filter by favorites
- [x] Banner ad integration

### Settings
- [x] Vibrate on scan toggle
- [x] Beep on scan toggle
- [x] Auto-open URL toggle
- [x] Copy on scan toggle
- [x] Rate App
- [x] Share App
- [x] Privacy Policy
- [x] Pro upgrade CTA
- [x] App version display

### Ads
- [x] App Open Ad
- [x] Interstitial (every 3 scans)
- [x] Banner (history screen)
- [x] Pro flag to disable ads

---

## 🌐 Play Store Listing

**App Title:** QR Code Scanner & Barcode Scanner – Fast QR Reader  
**Category:** Tools  
**Content Rating:** Everyone  
**Target SDK:** 34 (Android 14)  
**Min SDK:** 24 (Android 7.0)  
