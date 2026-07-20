# LifeOS integration

Workday Planner exposes a read-only, summaries-only ContentProvider for LifeOS.

- URI: `content://com.smithware.workdayplanner.summary/summary`
- Mutations: unsupported. `insert`, `update`, and `delete` are no-ops.
- Network: none added.
- Scope: current work/task/schedule summary only. It does not expose images,
  raw note text, full history, credentials, or private files.

Columns:

| Column | Meaning |
| --- | --- |
| `app_id` | Stable app key: `workday_planner` |
| `status` | Short human-readable state |
| `key_info` | Comma-separated summary counts |
| `alert` | Optional timely warning |
| `counts` | Pipe-separated compact counts |
| `due_soon` | Pipe-separated top task names or upcoming shifts |
| `last_updated` | Provider freshness text |
| `source` | Provider label |
