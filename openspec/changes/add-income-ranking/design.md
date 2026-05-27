## Context

本项目是一款个人记账 Android App（Java），使用 SQLite 直接访问，遵循 DAO 模式。最近刚完成"项目级别数据隔离"功能，支持按营业场所隔离交易数据。

StatisticsActivity 是现有的统计页面，支持按年/月/周/日筛选交易明细，并可按人员进一步过滤。用户希望在此基础上增加"收入排行榜"功能，快速查看指定时间段内谁带来的收入最多。

## Goals / Non-Goals

**Goals:**
- 在 StatisticsActivity 中增加收入排行榜视图，与现有明细视图通过 Spinner 切换
- 支持按日/周/月/年四个时间维度展示人员收入排行
- 排行榜按收入金额降序排列，展示排名、人员名、收入金额和笔数
- 前三名使用视觉高亮（金/银/铜）
- 与现有项目隔离机制兼容（只查询当前项目的交易）

**Non-Goals:**
- 不显示支出排行榜
- 不增加图表形式展示排行
- 不增加导出排行数据功能
- 不涉及后端服务或云端同步

## Decisions

### D1：视图切换方式
- **选择**：在 StatisticsActivity 顶部增加 `spinnerViewMode`（明细 / 排行）
- **理由**：与现有的 Period/Value/Person Spinner 风格一致，不占用额外垂直空间，实现简单
- **已考虑 alternative**：
  - TabLayout：更直观但占用额外垂直空间，在小屏手机上挤压列表区域
  - ToggleButton：位置醒目但需自定义样式匹配暗黑主题

### D2：排行数据聚合策略
- **选择**：在 TransactionDao 中使用 `rawQuery` + `GROUP BY person` + `SUM(amount)` + `ORDER BY SUM DESC`
- **理由**：SQLite 原生聚合查询效率高，避免内存中手动汇总
- **已考虑 alternative**：
  - 内存中遍历交易列表汇总：代码简单但大数据量时性能差

### D3：排行列表项数据展示
- **选择**：展示排名、人员名、总收入金额、收入笔数
- **理由**：排名和金额是核心信息，笔数提供额外参考价值
- **已考虑 alternative**：
  - 仅展示排名+金额：信息过少
  - 增加占比百分比：需要额外计算，超出当前需求

### D4：Person Spinner 在排行模式下的处理
- **选择**：排行模式下隐藏 `spinnerPerson`
- **理由**：排行本身就是按人员聚合，再按某个人筛选排行无意义
- **已考虑 alternative**：
  - 禁用但不隐藏：UI 上多一个不可交互元素，体验不佳

## Risks / Trade-offs

- **[Risk]** 大量交易数据时 GROUP BY 聚合查询可能变慢 → **Mitigation**：SQLite 在万级数据量下 GROUP BY 性能通常可接受；如未来遇到性能问题可添加索引
- **[Risk]** 用户可能困惑为何切换到排行模式后 Person Spinner 消失了 → **Mitigation**：通过视觉反馈（Spinner 隐藏动画）和布局一致性减少困惑
- **[Trade-off]** 只实现收入排行，不实现支出排行 → **接受理由**：用户明确只需要收入排行，支出排行当前无需求

## Migration Plan

N/A — 本 change 不涉及数据库 schema 变更或部署变更。纯功能新增，通过代码更新即可生效。

验收条件：
1. StatisticsActivity 可以切换到排行视图
2. 按日/周/月/年筛选时，排行列表正确聚合显示人员收入
3. 前三名有高亮颜色区分
4. 无收入数据时显示空状态提示
5. 编译通过，项目隔离功能不受影响

## Open Questions

无。
