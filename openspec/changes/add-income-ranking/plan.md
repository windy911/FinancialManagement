# 收入排行榜 Implementation Plan

> **For agentic workers:** Use superpowers:subagent-driven-development to implement this plan task-by-task.

**Goal:** 在 StatisticsActivity 中增加收入排行榜视图，支持按日/周/月/年维度查看人员收入排名。

**Architecture:** 新增 `PersonIncome` 数据类和 `TransactionDao` 聚合查询方法；StatisticsActivity 新增视图模式 Spinner 切换明细/排行；新增 `RankingAdapter` 和 `item_ranking.xml` 展示排名列表。

**Tech Stack:** Android (Java), SQLite, AndroidX, Material Components

---

## Task 1: 数据层 — PersonIncome + DAO 查询

**Files:**
- Create: `app/src/main/java/com/example/financialmanagement/model/PersonIncome.java`
- Modify: `app/src/main/java/com/example/financialmanagement/dao/TransactionDao.java`

- [ ] **Step 1: 创建 PersonIncome 数据类**

```java
package com.example.financialmanagement.model;

public class PersonIncome {
    private String name;
    private double totalIncome;
    private int count;

    public PersonIncome(String name, double totalIncome, int count) {
        this.name = name;
        this.totalIncome = totalIncome;
        this.count = count;
    }

    // getters
    public String getName() { return name; }
    public double getTotalIncome() { return totalIncome; }
    public int getCount() { return count; }
}
```

- [ ] **Step 2: 在 TransactionDao 中新增排行榜查询方法**

```java
public List<PersonIncome> getIncomeRankingByDate(String date, String project) {
    return getIncomeRanking(
        DatabaseHelper.COLUMN_DATE + " = ? AND " + DatabaseHelper.COLUMN_PROJECT + " = ?",
        new String[]{ date, project }
    );
}

public List<PersonIncome> getIncomeRankingByDateRange(String startDate, String endDate, String project) {
    return getIncomeRanking(
        DatabaseHelper.COLUMN_DATE + " >= ? AND " + DatabaseHelper.COLUMN_DATE + " <= ? AND "
        + DatabaseHelper.COLUMN_PROJECT + " = ?",
        new String[]{ startDate, endDate, project }
    );
}

public List<PersonIncome> getIncomeRankingByYearMonth(String yearMonth, String project) {
    return getIncomeRanking(
        DatabaseHelper.COLUMN_DATE + " LIKE ? AND " + DatabaseHelper.COLUMN_PROJECT + " = ?",
        new String[]{ yearMonth + "%", project }
    );
}

public List<PersonIncome> getIncomeRankingByYear(String year, String project) {
    return getIncomeRanking(
        DatabaseHelper.COLUMN_DATE + " LIKE ? AND " + DatabaseHelper.COLUMN_PROJECT + " = ?",
        new String[]{ year + "%", project }
    );
}

private List<PersonIncome> getIncomeRanking(String whereClause, String[] whereArgs) {
    List<PersonIncome> list = new ArrayList<>();
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    String sql = "SELECT " + DatabaseHelper.COLUMN_PERSON
            + ", SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") as total"
            + ", COUNT(*) as cnt"
            + " FROM " + DatabaseHelper.TABLE_TRANSACTIONS
            + " WHERE " + DatabaseHelper.COLUMN_TYPE + " = ? AND " + whereClause
            + " GROUP BY " + DatabaseHelper.COLUMN_PERSON
            + " ORDER BY total DESC";
    // prepend type to whereArgs
    String[] args = new String[whereArgs.length + 1];
    args[0] = Transaction.TYPE_INCOME;
    System.arraycopy(whereArgs, 0, args, 1, whereArgs.length);
    Cursor cursor = db.rawQuery(sql, args);
    if (cursor != null && cursor.moveToFirst()) {
        do {
            String name = cursor.getString(0);
            double total = cursor.getDouble(1);
            int count = cursor.getInt(2);
            list.add(new PersonIncome(name, total, count));
        } while (cursor.moveToNext());
        cursor.close();
    }
    return list;
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/financialmanagement/model/PersonIncome.java app/src/main/java/com/example/financialmanagement/dao/TransactionDao.java
git commit -m "feat: add income ranking query methods to TransactionDao"
```

---

## Task 2: UI 布局资源

**Files:**
- Modify: `app/src/main/res/layout/activity_statistics.xml`
- Create: `app/src/main/res/layout/item_ranking.xml`
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: 修改 activity_statistics.xml 增加 view mode Spinner**

在 `spinnerPeriod` 所在 LinearLayout 中增加一个 `spinnerViewMode`：

```xml
<Spinner
    android:id="@+id/spinner_view_mode"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginEnd="8dp" />
```

- [ ] **Step 2: 创建 item_ranking.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:padding="16dp"
    android:background="@drawable/bg_card"
    android:layout_margin="4dp">

    <TextView
        android:id="@+id/tv_rank"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:gravity="center"
        android:textSize="18sp"
        android:textStyle="bold"
        android:textColor="@color/on_surface" />

    <TextView
        android:id="@+id/tv_name"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:layout_marginStart="12dp"
        android:textSize="16sp"
        android:textColor="@color/on_surface"
        android:textStyle="bold" />

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:gravity="end">

        <TextView
            android:id="@+id/tv_amount"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="16sp"
            android:textColor="@color/income_green"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tv_count"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="12sp"
            android:textColor="@color/gray" />
    </LinearLayout>
</LinearLayout>
```

- [ ] **Step 3: 在 colors.xml 中添加排行高亮色**

```xml
<color name="rank_gold">#FFD700</color>
<color name="rank_silver">#C0C0C0</color>
<color name="rank_bronze">#CD7F32</color>
```

- [ ] **Step 4: 在 strings.xml 中添加排行相关字符串**

```xml
<string name="view_detail">明细</string>
<string name="view_ranking">排行</string>
<string name="no_income_data">暂无收入数据</string>
```

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res/layout/activity_statistics.xml app/src/main/res/layout/item_ranking.xml app/src/main/res/values/colors.xml app/src/main/res/values/strings.xml
git commit -m "feat: add ranking UI layouts and resources"
```

---

## Task 3: RankingAdapter

**Files:**
- Create: `app/src/main/java/com/example/financialmanagement/adapter/RankingAdapter.java`

- [ ] **Step 1: 创建 RankingAdapter**

```java
package com.example.financialmanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financialmanagement.R;
import com.example.financialmanagement.model.PersonIncome;

import java.util.ArrayList;
import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {

    private List<PersonIncome> items = new ArrayList<>();

    public void setItems(List<PersonIncome> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PersonIncome item = items.get(position);
        int rank = position + 1;
        holder.tvRank.setText(String.valueOf(rank));
        holder.tvName.setText(item.getName());
        holder.tvAmount.setText(String.format("+%.0f", item.getTotalIncome()));
        holder.tvCount.setText(item.getCount() + " 笔");

        // Top 3 highlight
        int colorRes;
        if (rank == 1) colorRes = R.color.rank_gold;
        else if (rank == 2) colorRes = R.color.rank_silver;
        else if (rank == 3) colorRes = R.color.rank_bronze;
        else colorRes = R.color.on_surface;
        holder.tvRank.setTextColor(holder.itemView.getContext().getColor(colorRes));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvAmount, tvCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvCount = itemView.findViewById(R.id.tv_count);
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/financialmanagement/adapter/RankingAdapter.java
git commit -m "feat: add RankingAdapter with top 3 highlight"
```

---

## Task 4: StatisticsActivity 集成

**Files:**
- Modify: `app/src/main/java/com/example/financialmanagement/StatisticsActivity.java`

- [ ] **Step 1: 添加 view mode Spinner 和 RankingAdapter 字段**

```java
private Spinner spinnerViewMode;
private RankingAdapter rankingAdapter;
private static final String VIEW_DETAIL = "明细";
private static final String VIEW_RANKING = "排行";
```

- [ ] **Step 2: 初始化 view mode Spinner**

在 `onCreate` 中，在 `spinnerPeriod` 初始化之后：

```java
spinnerViewMode = findViewById(R.id.spinner_view_mode);
List<String> viewModes = new ArrayList<>();
viewModes.add(VIEW_DETAIL);
viewModes.add(VIEW_RANKING);
ArrayAdapter<String> viewModeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, viewModes);
viewModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
spinnerViewMode.setAdapter(viewModeAdapter);

spinnerViewMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        updateViewMode();
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
});
```

- [ ] **Step 3: 初始化 RankingAdapter**

```java
rankingAdapter = new RankingAdapter();
```

- [ ] **Step 4: 实现 updateViewMode() 方法**

```java
private void updateViewMode() {
    String mode = (String) spinnerViewMode.getSelectedItem();
    if (VIEW_RANKING.equals(mode)) {
        spinnerPerson.setVisibility(View.GONE);
        loadRanking();
    } else {
        spinnerPerson.setVisibility(View.VISIBLE);
        updateStatistics();
    }
}
```

- [ ] **Step 5: 实现 loadRanking() 方法**

```java
private void loadRanking() {
    String period = (String) spinnerPeriod.getSelectedItem();
    String value = (String) spinnerValue.getSelectedItem();

    List<PersonIncome> ranking;
    if (PERIOD_YEAR.equals(period)) {
        ranking = transactionDao.getIncomeRankingByYear(value, currentProject);
    } else if (PERIOD_MONTH.equals(period)) {
        ranking = transactionDao.getIncomeRankingByYearMonth(value, currentProject);
    } else if (PERIOD_WEEK.equals(period)) {
        String startDate = value;
        String endDate = getSundayOfDate(value);
        ranking = transactionDao.getIncomeRankingByDateRange(startDate, endDate, currentProject);
    } else {
        ranking = transactionDao.getIncomeRankingByDate(value, currentProject);
    }

    recyclerView.setAdapter(rankingAdapter);
    rankingAdapter.setItems(ranking);

    // Clear summary text in ranking mode
    tvTotalIncome.setText(String.format(Locale.getDefault(), getString(R.string.total_income), 0.0));
    tvTotalExpense.setText(String.format(Locale.getDefault(), getString(R.string.total_expense), 0.0));
    tvBalance.setText(String.format(Locale.getDefault(), getString(R.string.balance), 0.0));

    if (ranking.isEmpty()) {
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setText(R.string.no_income_data);
        tvEmpty.setVisibility(View.VISIBLE);
    } else {
        recyclerView.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }
}
```

- [ ] **Step 6: 修改 updateStatistics() 切换 adapter**

在 `updateStatistics()` 方法开头，确保明细模式下使用 `adapter`：

```java
recyclerView.setAdapter(adapter);
```

- [ ] **Step 7: 在 updateStatistics() 和 loadRanking() 的调用点处理**

确保 `spinnerValue` 和 `spinnerPeriod` 的选择事件也触发正确的加载：

修改 `spinnerValue` 的 `onItemSelected`：
```java
spinnerValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String mode = (String) spinnerViewMode.getSelectedItem();
        if (VIEW_RANKING.equals(mode)) {
            loadRanking();
        } else {
            updateStatistics();
        }
    }
    @Override public void onNothingSelected(AdapterView<?> parent) {}
});
```

同样修改 `spinnerPeriod` 的 `onItemSelected`：
```java
@Override
public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    loadPeriodValues(periods.get(position));
    // loadPeriodValues already calls updateStatistics/loadRanking via spinnerValue selection
}
```

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/example/financialmanagement/StatisticsActivity.java
git commit -m "feat: integrate income ranking into StatisticsActivity"
```

---

## Task 5: 编译验证

- [ ] **Step 1: 编译项目**

```bash
gradle compileDebugJavaWithJavac
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: 修复编译错误（如有）**

- [ ] **Step 3: Commit**

```bash
git add .
git commit -m "fix: compilation fixes for income ranking feature"
```
