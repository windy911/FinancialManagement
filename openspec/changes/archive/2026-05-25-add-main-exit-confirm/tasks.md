## 1. 添加字符串资源

- [x] 1.1 在 `strings.xml` 中添加退出确认相关的字符串资源（标题、消息、确定、取消）

## 2. 实现退出确认逻辑

- [x] 2.1 在 `MainActivity` 中重写 `onBackPressed()` 方法
- [x] 2.2 使用 `AlertDialog.Builder` 创建确认对话框，引用 `strings.xml` 中的字符串资源
- [x] 2.3 验证点击"确定"退出应用，点击"取消"留在当前页面
