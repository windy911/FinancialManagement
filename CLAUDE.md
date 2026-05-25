# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean and rebuild
./gradlew clean assembleDebug

# Check compilation without producing APK
./gradlew compileDebugJavaWithJavac
```

No test suite is configured in this project. No linter is configured.

## Architecture

This is a single-module Android app (Java, minSdk 24, targetSdk 34) for personal financial record-keeping. It uses SQLite directly (no Room/ORM) and follows a simple DAO pattern.

### Data Layer

- **DatabaseHelper** — singleton `SQLiteOpenHelper` (version 4). Manages three tables: `transactions`, `persons`, `events`. Handles schema migrations in `onUpgrade()`. Must call `closeDatabase()`/`resetInstance()` before file-level DB operations (backup/restore).

  **Schema migration history**:
  | Version | Change |
  |---------|--------|
  | 1 → 2 | Added `persons` table |
  | 2 → 3 | Added `events` table; inserts 8 default events (餐饮, 交通, 购物, 娱乐, 工资, 奖金, 投资, 其他) on creation |
  | 3 → 4 | Added `avatar` column (TEXT, Base64 image string) to `persons` table |
- **DAO classes** (TransactionDao, PersonDao, EventDao) — each takes a Context, obtains the singleton DatabaseHelper, and provides CRUD + query methods. All DB access is **synchronous on the calling thread** with no background threading. UI code calls DAO methods directly.

### UI Layer

- **Adapters** (TransactionAdapter, PersonAdapter, EventAdapter) — back RecyclerViews throughout the app. TransactionAdapter delegates person avatar loading to AvatarHelper.
- **Activities** use standard Android patterns: MaterialToolbar, AlertDialog for confirmations, DatePickerDialog/TimePickerDialog in AddEditActivity, Spinner + AlertDialog for person/event selection pickers.

### Domain Model

Each Transaction has: type (income/expense), amount, person (name string FK to persons table), event (name string FK to events table), date (`yyyy-MM-dd`), time (`HH:mm`), and a millisecond timestamp for ordering.

Persons and Events are reference data managed by the user. Transactions reference them by name (not by ID).

### Screen Flow

- **MainActivity** — transaction list with summary totals; FAB to add; menu for backup/restore/export
- **AddEditActivity** — form to create/edit a transaction; person and event are selected from pickers backed by their DAOs
- **StatisticsActivity** — multi-filter statistics (by period type, period value, person) with text report generation (ReportHelper)
- **ChartActivity** — line chart (MPAndroidChart) showing daily income/expense trend over 7/30/90 days
- **PersonActivity / EventActivity** — CRUD for reference data

### Activity Communication Pattern

MainActivity communicates with AddEditActivity via the **legacy `startActivityForResult` / `onActivityResult` pattern** (not the modern Activity Result API):

```java
// MainActivity.java
private static final int REQUEST_ADD = 1;
private static final int REQUEST_EDIT = 2;

startActivityForResult(intent, REQUEST_ADD);   // for new transactions
startActivityForResult(intent, REQUEST_EDIT);  // for editing existing

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // Refresh transaction list
}
```

### Key Utilities

- **BackupHelper** — uses Storage Access Framework (SAF) for backup (.db file copy), restore (with validation and integrity check), and JSON export. No runtime permissions needed beyond SAF intents.

  **Critical**: Before any file-level DB operation, `BackupHelper` must call `DatabaseHelper.closeDatabase()` to release the file lock, then `DatabaseHelper.resetInstance()` afterward to recreate the singleton. Even on failure, `resetInstance()` is called to ensure the database connection can be re-established.
- **ReportHelper** — generates formatted text reports (daily/weekly/monthly) from filtered transaction lists, copyable to clipboard.
- **AvatarHelper** — generates text-based avatars (first character of name on a colored circle) or loads user-selected images. Stores images as Base64 strings in the `persons` table (`avatar` column).

### Dependencies

- AndroidX (AppCompat, Material, ConstraintLayout, RecyclerView)
- MPAndroidChart v3.1.0 (via JitPack) for line charts

### AndroidManifest & Permissions

- `READ_EXTERNAL_STORAGE` (maxSdk 32) and `READ_MEDIA_IMAGES` — used for avatar image selection via system picker.
- Backup/restore/export use Storage Access Framework (SAF) intents; no dedicated storage permissions needed.

### Conventions

- All UI strings are in Chinese (zh-CN). String resources are in `res/values/strings.xml`.
- Dark theme is the default (`Theme.FinancialManagement` in themes.xml); color palette defined in `colors.xml`.
- Date format throughout: `yyyy-MM-dd`. Time format: `HH:mm`.
- Gradle 8.0 wrapper, AGP 8.1.0, Java 8 source/target compatibility.
