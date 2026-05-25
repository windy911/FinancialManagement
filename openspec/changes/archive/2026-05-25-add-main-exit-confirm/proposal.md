## Why

用户在 MainActivity（首页）按系统返回键时，应用会直接退出，缺乏二次确认机制。这可能导致用户误触返回键而意外关闭应用，影响使用体验。增加退出确认对话框可以防止误操作，提升用户体验。

## What Changes

- 在 `MainActivity` 中拦截系统返回键（`onBackPressed`）
- 弹出 `AlertDialog` 确认对话框，提示用户确认是否退出应用
- 提供"确定"和"取消"两个选项
- 对话框文案使用中文，与现有 UI 语言保持一致
- 将退出确认文案添加到 `strings.xml` 资源文件中

## Capabilities

### New Capabilities

- `main-exit-confirm`: 首页返回键退出确认对话框，防止误触退出应用

### Modified Capabilities

（无现有 spec 需要修改）

## Impact

- **MainActivity.java**: 新增 `onBackPressed()` 重写逻辑
- **strings.xml**: 新增退出确认相关的字符串资源（标题、消息、按钮文案）
- **用户体验**: 按返回键退出应用时增加一次确认操作
