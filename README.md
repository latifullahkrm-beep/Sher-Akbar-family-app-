# Sher Akbar Family App — Native Android Studio Project

A complete native Android application built with **Kotlin**, **Jetpack Compose**, **Material 3**, **Room Database**, **Navigation Compose**, and **Firebase (Auth, Firestore, Storage, Messaging)** using **MVVM architecture**.

## Design & Heritage
- **Primary Color**: Deep Forest Green (`#064E3B`)
- **Secondary Color**: Royal Gold (`#D4AF37`)
- **Background**: Elegant Off-White (`#F8F7F2`) with full Material 3 Dark Mode support
- **Bilingual Support**: Complete English (`values/strings.xml`) & Urdu (`values-ur/strings.xml`) localization
- **Visual Motifs**: Custom Jetpack Compose `Canvas` Islamic 8-point geometric star lattice and Mughal arch borders, plus custom Sher Akbar family crest vector icon.

## Pre-Populated Demo Accounts (Offline / Local Mode)
To test immediately on install without Firebase setup:
- **Admin Login**: `admin@sherakbar.family` / `SherAkbar@2026`
- **Member Login**: `member@sherakbar.family` / `Family@2026`
*(You can also register new member accounts and approve them inside the Admin Panel.)*

## Building the Debug APK
1. Open the `android-app/` directory in **Android Studio Ladybug (2024.2)** or newer (JDK 17+).
2. Let Gradle sync all dependencies (`Room`, `Compose BOM`, `Navigation`, `Firebase BOM`, `Coil`, `Gson`).
3. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)**, or run from terminal inside `android-app/`:
   ```bash
   ./gradlew assembleDebug
   ```
4. The debug APK will be generated at:
   `app/build/outputs/apk/debug/app-debug.apk`

## Optional: Connecting Firebase for Online Sync
The app works immediately offline using **Room Database** with pre-populated 4-generation Sher Akbar family records. To enable cloud synchronization across devices:
1. Create a project in the [Firebase Console](https://console.firebase.google.com/).
2. Register an Android app with package name `com.sherakbar.family` (and `com.sherakbar.family.debug` if testing debug builds).
3. Download `google-services.json` and place it at `android-app/app/google-services.json`.
4. Enable **Firebase Authentication** (Email/Password), **Cloud Firestore**, and **Firebase Storage**.
5. Deploy `android-app/firestore.rules` to Firestore Rules.
6. Re-sync Gradle (`app/build.gradle.kts` automatically detects `google-services.json` and applies `com.google.gms.google-services`).
