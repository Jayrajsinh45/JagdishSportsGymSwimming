# Jagdish Sports Gym and Swimming

Android app for managing local gym and swimming memberships for personal use.

## Features

- Kotlin single-activity Android app
- Material Design 3 UI with Home and Report bottom tabs
- Gym and Swimming member lists
- Add, edit, and delete members
- Add member photos from camera or gallery, stored locally on the device
- Room SQLite database stored locally on the device
- Member status badges: Active, Expiring Soon, Expired
- Report screen with separate Gym and Swimming category views, counts, fees, and filters
- Month-wise report filter with separate Gym-only or Swimming-only PDF export
- Daily WorkManager check around 9:00 AM for memberships expiring today or in the next 3 days
- Local Android notifications, including Android 13+ notification permission request
- No login, Firebase, internet, or cloud database

## Project Details

- App name: `Jagdish Sports Gym and Swimming`
- Package: `com.jagdishsports.gymswimming`
- Language: Kotlin
- Minimum SDK: API 26
- Target SDK: API 34
- Database: Room over SQLite
- Background work: WorkManager

## Clone the Repository

```bash
git clone https://github.com/Jayrajsinh45/JagdishSportsGymSwimming.git
cd JagdishSportsGymSwimming
```

## Open in Android Studio

1. Open Android Studio.
2. Select **File > Open**.
3. Choose this project folder.
4. Let Android Studio sync Gradle.
5. Run the app on an emulator or Android device.

## Build Locally

```bash
./gradlew assembleDebug
```

On Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Test in a Web Browser

This project also includes a dependency-free web tester so you can try the membership screens without Android Studio.

```bash
npm start
```

Then open:

```text
http://localhost:3000
```

The web tester stores data in browser `localStorage`. It is meant for quick UI and workflow testing; the Android app continues to use Room SQLite on the device.

## Push to GitHub

```bash
git init
git add .
git commit -m "Initial Android app"
git branch -M main
git remote add origin https://github.com/Jayrajsinh45/JagdishSportsGymSwimming.git
git push -u origin main
```

## Download APK from GitHub Actions

1. Open the repository on GitHub.
2. Go to the **Actions** tab.
3. Open the latest **Build Android APK** workflow run.
4. Scroll to **Artifacts**.
5. Download `jagdish-sports-gym-swimming-debug-apk`.
6. Extract the zip to get `app-debug.apk`.

The workflow runs on every push to the `main` branch and uploads:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## APK Update Note

Version `1.3` keeps the same package name and stable debug signing key used from version `1.1`, with a higher `versionCode`, so GitHub Actions debug APKs from version `1.1` onward can update over each other. The Room database migrates existing members safely and adds an optional photo field without deleting old data. If Android refuses to update an APK installed from an older Actions run, uninstall that old build once, then install the new APK.
