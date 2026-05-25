## Context

当前 `MainActivity` 作为应用入口和主页面，用户在浏览交易列表时按系统返回键会直接退出应用。这在以下场景可能导致误操作：用户快速滑动浏览时不小心触碰到返回键；用户在单手操作时误触。Android 系统的默认行为没有提供二次确认机制。

## Goals / Non-Goals

**Goals:**
- 在 `MainActivity` 拦截返回键事件，弹出确认对话框
- 对话框文案使用中文，风格与现有暗色主题一致
- 用户点击"确定"后退出应用，点击"取消"后留在当前页面
- 将字符串资源提取到 `strings.xml`，支持后续国际化

**Non-Goals:**
- 不修改其他 Activity（AddEditActivity、StatisticsActivity 等）的返回行为
- 不增加"不再提示"等复杂逻辑
- 不影响 Home 键、最近任务键等系统导航行为

## Decisions

- **使用 `AlertDialog.Builder` 而非自定义 Dialog**：与现有代码中删除确认、恢复确认等对话框保持一致的 UI 模式（`MainActivity.showDeleteDialog()`、`MainActivity.showRestoreConfirmDialog()`）。降低维护成本，用户交互习惯一致。
- **重写 `onBackPressed()` 而非注册 `OnBackPressedDispatcher`**：项目使用 minSdk 24，`OnBackPressedDispatcher` 需要 AndroidX Activity 1.6.0+，当前 `AppCompatActivity` 的 `onBackPressed()` 方式更简单直接，且与现有代码风格一致。
- **文案使用中文**：遵循项目现有约定（所有 UI 字符串均为中文）。

## Risks / Trade-offs

- **[Risk]** 增加一步操作，部分用户可能觉得繁琐 → **Mitigation**：仅在 MainActivity 生效，其他页面不受影响；这是一个常见的移动端防误触设计
- **[Risk]** `onBackPressed()` 在 API 33+ 被标记为弃用 → **Mitigation**：对于 minSdk 24 且使用传统 Activity 模式的项目，这是目前最兼容的做法。如果未来升级到新版 AndroidX Activity，可以迁移到 `OnBackPressedDispatcher`
