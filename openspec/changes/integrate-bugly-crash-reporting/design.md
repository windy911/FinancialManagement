# Design: 接入 Bugly 崩溃监控

## 技术方案

### SDK 选择

使用腾讯 Bugly crashreport 最新稳定版（`com.tencent.bugly:crashreport:latest.release`）。

### 初始化方式

新建 `FinancialApplication extends Application`，在 `onCreate()` 中调用 `CrashReport.initCrashReport()`。

App ID 通过 AndroidManifest.xml 的 `<meta-data>` 配置，便于不同环境切换。

### 权限配置

在 AndroidManifest.xml 中添加：
- `android.permission.INTERNET` — 崩溃数据上报
- `android.permission.ACCESS_NETWORK_STATE` — 网络状态检测
- `android.permission.ACCESS_WIFI_STATE` — WiFi 状态检测

### 文件变更

| 文件 | 操作 | 说明 |
|------|------|------|
| `build.gradle` (project) | 修改 | 无需修改，已有 mavenCentral |
| `app/build.gradle` | 修改 | 添加 Bugly SDK 依赖 |
| `AndroidManifest.xml` | 修改 | 添加权限声明、Application 指定、App ID meta-data |
| `FinancialApplication.java` | 新增 | 自定义 Application，初始化 Bugly |

### 初始化代码

```java
public class FinancialApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext());
    }
}
```

Bugly 会自动从 AndroidManifest.xml 读取 meta-data 中的 `BUGLY_APPID`。

### 风险评估

- 风险低：Bugly 是成熟的第三方 SDK，接入方式标准
- 回退方案：移除依赖和初始化代码即可完全移除
