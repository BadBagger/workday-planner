# Workday Planner

Native Android work planner built with Kotlin, Jetpack Compose, local storage, notifications, repeating tasks, and schedule screenshot import.

## Features

- Task list with deadlines, notes, alarms, completion, and deletion
- Repeating tasks: none, daily, weekdays, or weekly
- Days off tracked manually or imported from schedule text
- Screenshot import using Google ML Kit on-device text recognition, no API key or paid service
- Local-only storage with `SharedPreferences`
- Compose Navigation across Tasks, Schedule, Import, and Task detail screens
- Android notifications for task alarms

## Run In Android Studio

1. Open this folder in Android Studio.
2. Let Android Studio install/sync the requested SDK, Gradle, and JDK if prompted.
3. Select an emulator or Android device.
4. Run the `app` configuration.

## Command Line

From this directory:

```powershell
.\gradlew.bat :app:check
.\gradlew.bat :app:assembleDebug
```

This project uses Gradle `9.4.1`, Android Gradle Plugin `9.2.1`, Kotlin `2.2.10`, and Compose BOM `2026.06.01`.

## Schedule Import Format

The import screen accepts screenshots or pasted text. The parser recognizes common lines like:

```text
7/6 9:00 AM - 5:30 PM
7/7 OFF
07-08-2026 8am - 4pm
```

Unparsed OCR lines remain visible so they can be corrected before applying the import.
