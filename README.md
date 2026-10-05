# Workday Planner

Native Android work planner built with Kotlin, Jetpack Compose, local storage, notifications, repeating tasks, and schedule screenshot import.

## Features

- Today dashboard for shifts, tasks, reminders, timecard, and watch-outs
- Task list with deadlines, notes, alarms, completion, and deletion
- Repeating tasks: none, daily, weekdays, weekly, every workday, or custom days
- Days off tracked manually or imported from schedule text
- Built-in work checklist/task templates stay free; Premium is limited to convenience and power-user tools
- Screenshot import using Google ML Kit on-device text recognition, no API key or paid service
- Local-only storage with `SharedPreferences`
- Compose Navigation across Today, Notes, Schedule, Manager, Settings, Import, and task detail screens
- Full task/shift alarm support with local app alarms and system Clock handoff where available
- Live Todoist sync for to-dos, repeating tasks, and dated plans

## Todoist

Workday Planner can keep the To-do tab in sync with the Todoist account you connect. Sync covers every project on that account, and it refreshes when you open the app and about every three minutes while the app is open. New tasks and completions made in the app are written back. Dated tasks and events also show on the Schedule tab.

Connect from **Settings → Todoist**:

1. **Connect with Todoist** opens Todoist in the browser and asks you to allow access. No client secret is stored in the app. The sign-in uses a public OAuth client registered on your phone, with PKCE.
2. Or paste an API token from Todoist: **Settings → Integrations → Developer**. The token stays on this phone.

Shift-only repeats (opening, closing, truck, and inventory) stay in Workday Planner because Todoist has no matching rule. Daily, weekday, weekly, and custom-day repeats sync.

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
