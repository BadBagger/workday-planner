# Workday Planner Improvement Backlog

Research baseline:
- Shift calendar competitors emphasize fast shift entry, calendar views, reminders, widgets, notes per day, and repeating shift patterns.
- Team scheduling competitors add communication, time tracking, availability, and time-off workflows, but those are heavier than Workday Planner needs right now.
- Task competitors win through fast capture, recurring task rules, calendar visibility, reminders, and simple completion flows.

## Product Direction

Workday Planner should stay focused as a simple workday command center for hourly and shift workers:
- Am I working today?
- What is my next shift?
- What tasks are due before or during work?
- What events or meetings are coming up?
- Can I import my schedule without fighting the app?

## Phase 1 - Widget Polish

- [x] Replace text checkbox glyphs with a more polished visual treatment.
- [ ] Add completed-task feedback after tapping a widget task, such as a brief checked state before it disappears.
- [ ] Improve small-widget layout so title, next shift, and first task do not feel cramped.
- [x] Add widget size presets:
  - Compact: next shift + 1 task
  - Standard: next shift + 3 tasks
  - Large: next shift + tasks + next event
- [ ] Add a widget setting to choose what appears first:
  - Tasks first
  - Schedule first
  - Balanced
- [ ] Consider adding a separate "Today Tasks" widget that only shows checkable work tasks.

## Phase 2 - Task System

- [x] Add daily work notes with local smart organization.
- [x] Add note filters and note-to-task conversion.
- [x] Add note search and smart note insight counts.
- [x] Add custom repeat days for tasks in a cleaner UI, using weekday chips instead of only dropdown choices.
- [x] Add task categories:
  - Orders
  - Cleaning
  - Prep
  - Admin
  - Personal
- [ ] Add priority levels with subtle visual indicators.
- [x] Add "due before shift starts" as a quick task deadline option.
- [ ] Add task templates for repeated work routines.
- [x] Add a "Today's work tasks" filter on the Tasks screen.
- [ ] Add overdue and upcoming sections.
- [ ] Add optional task duration so tasks can appear more clearly in day planning.

## Phase 3 - Schedule Calendar

- [ ] Improve the schedule screen into a richer calendar-first layout.
- [ ] Add week navigation controls:
  - Previous week
  - Today
  - Next week
- [ ] Add month view with dots or colored blocks for work days, days off, events, and tasks.
- [ ] Add shift detail cards when tapping a calendar day.
- [ ] Add shift templates:
  - Morning
  - Mid
  - Close
  - Custom
- [ ] Add split-shift support.
- [ ] Add unpaid break/rest time support.
- [ ] Add total scheduled hours by week and month.
- [ ] Add optional estimated pay summary later, if the user wants it.

## Phase 4 - Import Flow

- [ ] Make Import a clear 4-step flow:
  - Pick screenshot
  - Crop/confirm schedule area
  - Review parsed shifts
  - Apply import
- [ ] Add an import confidence indicator per parsed row.
- [ ] Let users edit a parsed row before applying:
  - Date
  - Start time
  - End time
  - Role
  - Store
  - Day off
- [ ] Add "merge with existing schedule" vs "replace selected week" options.
- [ ] Show exactly which dates will be changed before applying.
- [ ] Add support for importing multiple screenshots back-to-back.
- [ ] Add better unresolved-line handling:
  - Assign as shift
  - Assign as day off
  - Assign as role/store
  - Ignore

## Phase 5 - Events And Navigation

- [ ] Let events be attached to a shift or standalone.
- [ ] Show work events differently from tasks and shifts.
- [ ] Add event types:
  - Meeting
  - Training
  - Appointment
  - Deadline
- [ ] Add location autocomplete only if a free/local-friendly option is available.
- [ ] Keep the current simple "Navigate" button that opens Maps.
- [ ] Add "leave by" reminder based on event/shift start time.

## Phase 6 - Reminders

- [ ] Add reminder presets:
  - At shift start
  - 10 minutes before
  - 30 minutes before
  - 1 hour before
  - Custom
- [ ] Add reminder bundles for shift days:
  - Wake up
  - Leave for work
  - Task deadline
- [ ] Add a snooze action to notifications.
- [x] Make notifications open the exact task, event, or shift detail.

## Phase 7 - Premium Feel

- [ ] Add a stronger home/dashboard screen above Tasks, Schedule, and Import.
- [ ] Refine typography and spacing across all screens.
- [ ] Add theme previews for Classic Blue, Emerald, and Sunrise.
- [ ] Add widget preview examples inside the app.
- [ ] Add polished empty states with clear actions.
- [ ] Replace all prototype/debug labels with user-facing language.
- [ ] Add onboarding for first run:
  - Add first task
  - Import schedule
  - Add widget
  - Enable notifications

## Phase 8 - Reliability

- [ ] Add parser unit tests for every real OCR screenshot pattern seen so far.
- [x] Add local smart note organizer unit tests.
- [ ] Add widget update tests where practical.
- [ ] Add backup/export of local planner data.
- [ ] Add import undo for the last schedule import.
- [ ] Add clear version notes in the app settings screen.

## Next Recommended Build

Start with Phase 1 and Phase 2:
1. Finish the checkbox widget visual polish.
2. Add task categories and cleaner custom weekday repeat chips.
3. Add a Today Tasks section so work tasks are easier to scan.
4. Add widget display presets.

That set improves the daily-use experience without taking on risky calendar/import rewrites first.
