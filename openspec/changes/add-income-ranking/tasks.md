## 1. 数据层 — 排行榜查询

- [x] 1.1 创建 `PersonIncome` 数据类（name, totalIncome, count）
- [x] 1.2 在 `TransactionDao` 中新增按日收入排行榜查询方法
- [x] 1.3 在 `TransactionDao` 中新增按周收入排行榜查询方法
- [x] 1.4 在 `TransactionDao` 中新增按月/按年收入排行榜查询方法

## 2. UI 布局资源

- [x] 2.1 修改 `activity_statistics.xml` — 在 `spinnerPeriod` 旁新增 `spinnerViewMode`
- [x] 2.2 创建 `item_ranking.xml` — 排行列表项布局（排名、人员名、金额、笔数）
- [x] 2.3 在 `colors.xml` 中添加金/银/铜色（rank_gold, rank_silver, rank_bronze）
- [x] 2.4 在 `strings.xml` 中添加排行相关字符串（view_detail, view_ranking, no_income_data）

## 3. 适配器

- [x] 3.1 创建 `RankingAdapter` — 绑定 `PersonIncome` 列表到 RecyclerView
- [x] 3.2 实现前三名金/银/铜色高亮显示逻辑

## 4. StatisticsActivity 集成

- [x] 4.1 添加 `spinnerViewMode` 初始化和选择事件监听
- [x] 4.2 实现明细/排行视图切换逻辑（显示/隐藏 Person Spinner、切换 RecyclerView 数据源）
- [x] 4.3 实现排行数据加载方法（根据 Period + Value 调用对应 DAO 方法）
- [x] 4.4 实现排行空状态处理（无收入时显示提示）
- [x] 4.5 确保排行查询使用 `currentProject` 过滤

## 5. 验证

- [x] 5.1 编译项目确保无编译错误
- [x] 5.2 验证按日/周/月/年各维度的收入排行功能正常
- [x] 5.3 验证前三名高亮颜色正确显示
- [x] 5.4 验证项目隔离功能不受影响
