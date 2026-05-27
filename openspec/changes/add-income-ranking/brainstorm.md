<!--
Raw capture of superpowers:brainstorming output.

本檔原樣捕捉 brainstorming skill 的產出，不強制結構。
Skill 的自然產出通常是 decision log 格式（背景 → 決議鏈 Q1-Qn → 設計取捨），
但依對話內容可能有不同組織方式。

design.md 從本檔萃取並重新整理為結構化設計文件。

不要將本檔的內容複製到 design.md — design.md 是獨立的重組產物，
兩者互補但不重疊。
-->

# Brainstorm: 收入排行榜功能

## 背景

用户希望在记账 App 中增加收入排行榜功能，按人员维度展示谁的收入最多。项目隔离功能刚刚完成，当前有 StatisticsActivity 统计页面（支持按日/周/月/年筛选 + 按人员筛选）和 ChartActivity 图表页面。

## 需求澄清

**Q1: 排行榜的排行维度是什么？**
→ 按人员排名，看谁的收入最多。

**Q2: 排行榜放在哪里展示？**
选项：A) StatisticsActivity 中增加切换标签 / B) 新建独立页面 / C) ChartActivity 中增加区域
→ 选择 A：在统计页面中增加切换。

**Q3: 时间维度？**
用户强调需要日/周/月排行榜。
→ 复用现有的 Period Spinner（按年/按月/按周/按日），四个维度都支持排行。

## 方案对比

| 方案 | 描述 | 优点 | 缺点 | 结果 |
|------|------|------|------|------|
| A: Spinner 切换 | 新增视图模式 Spinner（明细/排行），切换列表内容 | 与现有 Spinner 风格一致，不占用额外垂直空间，实现简单 | 切换不如 Tab 直观 | ✅ 选中 |
| B: TabLayout 切换 | 底部增加 Material TabLayout（明细/排行） | 切换直观 | 占用额外垂直空间 | ❌ |
| C: ToggleButton 切换 | 统计卡片下方增加 ToggleButton | 位置醒目 | 需自定义样式 | ❌ |

## 最终设计决策

### 数据层
- `TransactionDao` 新增聚合查询方法，按人员汇总指定时间段内的总收入
- 使用 `rawQuery` + `GROUP BY person` + `SUM(amount)` + `ORDER BY SUM DESC`
- 新增 `PersonIncome` 数据类（name + totalIncome + count）
- 继续复用 `project` 过滤（与现有项目隔离兼容）

### UI 层
- StatisticsActivity 新增 `spinnerViewMode`（明细 / 排行）
- 排行模式隐藏 `spinnerPerson`（人员筛选对排行无意义）
- 新增 `RankingAdapter` + `item_ranking.xml`
- 排行列表项展示：排名、人员名、收入金额、笔数
- 前三名用不同颜色高亮（金/银/铜）

### 交互流程
1. 用户选择 Period（如"按月"）和 Value（如"2026-05"）
2. 切换 ViewMode 为"排行"
3. 查询该时间段内所有人员的收入汇总，按金额降序排列
4. 如果所有人收入为 0，显示"暂无收入数据"

### 范围确认
- ✅ 只显示收入排行榜（不显示支出）
- ✅ 支持日/周/月/年四个维度
- ✅ 与项目隔离兼容
- ❌ 不需要图表展示排行
- ❌ 不需要导出排行数据
