# 项目级别数据隔离设计

## 背景

当前 App 所有交易记录都在一个全局账本中。用户需要按营业场所（如 A店、B店）隔离数据，不同场所只能看到各自的记录。

## 目标

- 支持多营业场所（项目）维度
- 打开 App 直接进入上次使用的场所
- 在 MainActivity 内通过 Toolbar 下拉切换场所
- 交易数据按场所完全隔离（查询、统计、图表）
- 人员（Person）和事件（Event）全局共享
- 备份/恢复保持全局行为不变

## 非目标

- 不实现跨项目数据汇总
- 不实现项目权限控制
- 不改变人员/事件的共享属性
- 不改变现有备份恢复逻辑

## 架构设计

### 数据库层

**Schema 变更（version 4 → 5）：**

```sql
-- 新增 projects 表
CREATE TABLE projects (
    project_id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_name TEXT NOT NULL UNIQUE,
    project_created_at INTEGER NOT NULL
);

-- Transaction 表新增 project 列
ALTER TABLE transactions ADD COLUMN project TEXT NOT NULL DEFAULT '默认项目';
```

**迁移策略（onUpgrade 4→5）：**
1. 创建 `projects` 表
2. 插入"默认项目"
3. 执行 `UPDATE transactions SET project = '默认项目'`

### 模型层

- **Project.java**：`id`, `name`, `createdAt`（与 Person/Event 同模式）
- **Transaction.java**：新增 `project` 字段 + getter/setter

### 数据访问层

- **ProjectDao.java**：CRUD + `getAll()`（与 PersonDao/EventDao 同模式）
- **TransactionDao.java**：所有查询方法增加 `project` WHERE 过滤条件

### UI 层

**MainActivity Toolbar：**
- 标题区域替换为 Spinner 下拉选择器
- 显示当前项目列表，选中当前项目
- 切换时保存到 SharedPreferences 并刷新数据

**ProjectActivity（新增）：**
- 复用 PersonActivity/EventActivity 模式
- RecyclerView + ProjectAdapter + FAB 添加
- 支持增删改项目

**menu_main.xml：**
- 新增"项目管理"菜单项 → 跳转 ProjectActivity

### 本地存储

```java
SharedPreferences sp = getSharedPreferences("app_prefs", MODE_PRIVATE);
sp.edit().putString("current_project", projectName).apply();
```

启动时读取，默认"默认项目"。

## 边界情况处理

| 场景 | 处理 |
|------|------|
| 删除当前正在使用的项目 | 不允许删除，Toast 提示"无法删除当前使用的项目" |
| 删除有交易关联的项目 | 允许删除，该项目下的交易保留但不再显示 |
| 所有项目都被删除 | 自动创建"默认项目"，切换过去 |
| 首次安装（无项目） | onCreate 自动插入"默认项目" |
| 升级（v4→v5，有旧数据） | onUpgrade 创建"默认项目"，旧交易全归到该项目 |

## 文件变更清单

| 类型 | 文件 |
|------|------|
| 新增模型 | `Project.java` |
| 新增 DAO | `ProjectDao.java` |
| 新增 Activity | `ProjectActivity.java` |
| 新增 Adapter | `ProjectAdapter.java` |
| 修改 | `DatabaseHelper.java`（v5 迁移） |
| 修改 | `Transaction.java`（+project 字段） |
| 修改 | `TransactionDao.java`（+project 过滤） |
| 修改 | `MainActivity.java`（+Spinner + SP） |
| 修改 | `AddEditActivity.java`（+project 写入） |
| 新增布局 | `activity_project.xml`, `item_project.xml` |
| 修改布局 | `activity_main.xml`（Toolbar 加 Spinner） |
| 修改资源 | `strings.xml`, `AndroidManifest.xml`, `menu_main.xml` |
