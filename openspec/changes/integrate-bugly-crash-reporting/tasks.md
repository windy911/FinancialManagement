## 1. 添加 Bugly SDK 依赖

- [x] 1.1 在 `app/build.gradle` 的 dependencies 中添加 `implementation 'com.tencent.bugly:crashreport:latest.release'`

## 2. 配置 AndroidManifest.xml

- [x] 2.1 添加网络权限声明（INTERNET、ACCESS_NETWORK_STATE、ACCESS_WIFI_STATE）
- [x] 2.2 在 `<application>` 标签中指定 `android:name=".FinancialApplication"`
- [x] 2.3 在 `<application>` 内添加 `<meta-data>` 配置 Bugly App ID 和 App Key

## 3. 创建 Application 类

- [x] 3.1 新建 `FinancialApplication.java`，继承 `Application`
- [x] 3.2 在 `onCreate()` 中调用 `CrashReport.initCrashReport(getApplicationContext())`

## 4. 验证

- [x] 4.1 编译项目确保无编译错误
- [ ] 4.2 验证 Logcat 中出现 Bugly 初始化日志
