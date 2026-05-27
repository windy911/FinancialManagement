## ADDED Requirements

### Requirement: 首页返回键退出确认
用户在 MainActivity（首页）按系统返回键时，系统 SHALL 弹出确认对话框，询问用户是否确认退出应用。

#### Scenario: 用户按返回键触发确认对话框
- **WHEN** 用户在 MainActivity 按下系统返回键
- **THEN** 系统显示一个 AlertDialog，标题为"确认退出"，消息为"确定要退出应用吗？"，包含"确定"和"取消"两个按钮

#### Scenario: 用户确认退出
- **WHEN** 用户在退出确认对话框中点击"确定"按钮
- **THEN** 系统关闭对话框并退出应用

#### Scenario: 用户取消退出
- **WHEN** 用户在退出确认对话框中点击"取消"按钮
- **THEN** 系统关闭对话框，用户留在 MainActivity，不退出应用