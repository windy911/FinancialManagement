## Why

当前 StatisticsActivity 只能按时间段和人员筛选查看交易明细列表，用户无法快速了解"在某个时间段内，哪个人带来的收入最多"。随着交易数据量增加，手动从明细中汇总判断变得越来越不方便。增加收入排行榜功能可以让用户一眼看到收入贡献排名，提升数据洞察力。

## What Changes

**StatisticsActivity 视图模式**
- From：只有一个明细视图，展示交易列表
- To：增加"明细/排行"视图切换，明细视图保持现有行为，排行视图展示人员收入排行
- Reason：用户需要快速查看收入排名
- Impact：非破坏性变更，现有功能不受影响

**TransactionDao 查询能力**
- From：只有交易明细查询方法
- To：新增按人员聚合收入的排行榜查询方法（按日/周/月/年）
- Reason：排行需要聚合数据支撑
- Impact：非破坏性变更，新增方法不影响现有方法

**UI 新增**
- 新增 RankingAdapter 和 item_ranking.xml 布局
- 排行列表项展示：排名、人员名、收入金额、笔数
- 前三名使用金/银/铜色高亮

## Capabilities

### New Capabilities
- `income-ranking`: 按人员展示指定时间段内的收入排行榜，支持日/周/月/年维度，前三名高亮显示

### Modified Capabilities
- 无现有 spec 的需求变更

## Impact

- **修改文件**：StatisticsActivity.java、TransactionDao.java、activity_statistics.xml、strings.xml
- **新增文件**：RankingAdapter.java、item_ranking.xml、PersonIncome.java
- **依赖**：复用现有 TransactionDao、PersonDao 和项目隔离机制
- **无外部依赖变更**
