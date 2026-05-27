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

## 变更工作流（Claude Code 启动先读）

本 repo 采用 [`superpowers-bridge`](./openspec/schemas/superpowers-bridge) 衔接 OpenSpec 与 Superpowers。整合规则（语言、artifact 路径、PRECHECK）以该 bridge README 为准；以下是给 Claude 的路由指引。

### 入口分流

| 你看到的触发  | 应该怎么做                |
|---------------------------------------------------|----------------------------------------------------------------------------------------------------------|
| 用户以 narrative 开「设计讨论 / 头脑风暴 / 调研 / 疑问 / bug fix」  | 先 verbal `superpowers:brainstorming`，**不**写到 `docs/superpowers/specs/`；对话收敛后依下方 5 条判准升级到 `/opsx:propose` |
| 用户直接调用 `/opsx:new` / `/opsx:ff` / `/opsx:propose` | 走 schema 既定流程；artifact instruction 会在每步注入                                                                |
| 用户明确说 typo / config 微调 / 文档更新  | 尝试直接 PR，**不**建 change（见下方 skip 规则）                                                                       |
| 已经在某个 change 中  | `/opsx:continue` 或 `/opsx:apply` / `/opsx:verify` / `/opsx:archive` 推进                                   |

### 何时**不**走 opsx（尝试直接 PR，需询问开发者）

| 情境                                                               | 直接 PR？            |
|------------------------------------------------------------------|-------------------|
| 新功能 / 新 capability / 架构变更 / breaking change / Bug fix（变更或补缺spec） | ❌ 要走 opsx         |
| 测试补写 / linter 规则 / 非破坏性升级 / typo / 文档 / config 值微调               | ✅ 尝试直接 PR（需询问开发者） |

原则：
- **流程仪式跟风险成正比**。
  - 动到对外合约 / schema / 跨系统对接 / 合规边界 → opsx；
  - 其他 → 询问开发者走 opsx 还是 直接修改。
- bug fix 也要看情况：
  - 如果是功能性 bug（功能不符合 spec，或spec有欠缺） → opsx；
  - 如果是实现细节 bug（不涉及 spec 变更） → 直接 PR，建议询问是否总结相关经验沉淀下来。

### Verbal brainstorm 升级到 opsx 的 5 条判准

5 条**全满足**才升级（任一缺则继续 brainstorm，不写到 `docs/superpowers/specs/`）：

1. **Scope 锁定** —— 一句话讲清「包含 / 不包含什么」
2. **主要设计分歧已收敛** —— 替代方案选过，剩下 TBD 有明确 owner 与影响面
3. **跨系统依赖盘点过** —— 对方就绪 / 暂 mock / 真未知，三选一讲得清
4. **验收条件可陈述** —— 具体 pass 条件（例：`./mvnw clean verify` 通过 + N 个成果）
5. **对话进入收敛** —— 最近几轮在 confirm 不在发散

全满足 → 主动建议用户「要不要 `/opsx:propose`？」，用户 ack 后落地。永远不要自动触发。

### Front-door 反模式（别做）

- 让 brainstorming 写到 `docs/superpowers/specs/`
- 让 writing-plans 写到 `docs/superpowers/plans/`
- TBD 没收敛就升级到 opsx
- 对 bug fix / typo 也建 change

详细见 [superpowers-bridge README §进入与离开的判断](./openspec/schemas/superpowers-bridge/README.md#entry--exit-gates)。

### 知识、规则、约束
- **.wiki/knowledge/** — 业务背景、业务功能、项目结构、架构设计、技术模块、决策经验、踩坑经验等
- **.wiki/patterns/** — 各场景代码模式（含代码示例）
- **.claude/rules/** — 开发规范、代码风格、设计原则等
- **.claude/skills/** — 编码技能（如代码审查、单测编写、性能优化等）, 代码模板，开发工具使用指南等
- **openspec** — 规范文档（spec.md）与代码实现的同步工具，确保每个实现都有对应的 spec.md 文档，并且代码与文档保持同步。

### 上下文原则
1. 优先查阅.wiki目录下文档（必读.wiki/README.md），确认已有知识和经验，避免直接查阅代码。
2. 结合历史openspec，了解相关设计决策和实现细节，避免重复踩坑。
3. 有价值的发现（新知识、新经验、已有的模式、缺漏的知识、犯的错误等等） → 主动建议沉淀到[上下文相关目录](#知识规则约束)
4. 注意维护知识库的质量，确保内容准确、清晰、易于理解、及时更新，避免过时或错误的信息误导后续开发者。


## 核心法则

### Spec 驱动（Code is Cheap, Context is Expensive）
代码是廉价的消耗品，文档（Spec）才是昂贵的核心资产。
1. **No Spec, No Code** — 没有 spec，不准写代码
2. **Spec is Truth** — spec 和代码冲突时，错的一定是代码
3. **Reverse Sync** — 执行中发现 spec 与实际不符，先修 spec 再修代码
4. **代码现状必须有出处** — 每个结论必须标注文件路径和类名/方法名，不接受"我认为"、"通常来说"
5. **变更即记录** — 任何代码变更完成后都必须同步更新对应的需求目录下的文档