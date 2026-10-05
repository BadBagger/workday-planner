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
- To Do pushes tasks, events, schedules, goals, files, and the Deli Daily Standards sheet into the planner

## To Do

Workday Planner keeps the plan. Todoist is not used. The companion assistant To Do, signed with the same key as this app, writes straight into:

`content://com.smithware.workdayplanner.todo/items`

A personal build uses `content://com.smithware.workdayplanner.personal.todo/items`.

Each insert is a set of fields. `kind` is `task`, `todo`, `work`, `event`, `goal`, `schedule`, `file`, or `standards`. `action` is `create`, `update`, `complete`, or `delete`. The To-do list and schedule read the same saved plan, so a push shows up while the app is open.

A schedule is `kind=schedule` and `text` set to the schedule wording, including a Passport screenshot once it has been read as text. Shifts and days off land on the Schedule tab. A goal is `kind=goal` with `title`, `focus`, `target`, and `daily_requirements` (one requirement per line). A file or photo is `kind=file` with `title`, `path`, and `mime`. Photos and documents can be searched from the notes images and the saved file list.

Deli Daily Standards is `kind=standards`. Use `page=daily` with `date`, `line`, `done`, `owner`, `time`, `initials`, `why`, and `who` for one of the 11 objectives. Use `page=agreements` with `item` and `answer` once something has been agreed. Use `page=tracker` with `date`, `column`, and `mark` (`Y` or `N`). The sheet is on the To-do tab and the Schedule tab. Kyle does not type it in.

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
