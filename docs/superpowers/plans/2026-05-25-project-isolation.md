# 项目级别数据隔离 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为记账 App 增加营业场所（项目）维度，实现交易数据按项目隔离，支持 Toolbar 下拉切换。

**Architecture:** 新增 `projects` 表，Transaction 表新增 `project` 字段（TEXT 外键，与 person/event 同模式）。MainActivity Toolbar 替换为 Spinner 下拉选择当前项目，SharedPreferences 记住上次选择。新增 ProjectActivity/ProjectAdapter 管理项目 CRUD。

**Tech Stack:** Android (Java), SQLite, AndroidX, Material Components

---

## File Structure

| File | Action | Responsibility |
|------|--------|---------------|
| `DatabaseHelper.java` | Modify | v4→v5 迁移：新增 projects 表，Transaction 加 project 列 |
| `Transaction.java` | Modify | 新增 `project` 字段 |
| `Project.java` | Create | 项目模型（id, name, createdAt） |
| `ProjectDao.java` | Create | 项目 CRUD + getAll() |
| `TransactionDao.java` | Modify | 所有查询增加 project WHERE 过滤 |
| `MainActivity.java` | Modify | Toolbar Spinner + SharedPreferences + 项目切换刷新 |
| `AddEditActivity.java` | Modify | 保存交易时写入当前 project |
| `ProjectActivity.java` | Create | 项目 CRUD 页面（复用 PersonActivity 模式） |
| `ProjectAdapter.java` | Create | RecyclerView Adapter（复用 PersonAdapter 模式，无头像） |
| `activity_main.xml` | Modify | Toolbar 标题替换为 Spinner |
| `activity_project.xml` | Create | 项目列表页面布局 |
| `item_project.xml` | Create | 项目列表项布局 |
| `menu_main.xml` | Modify | 新增"项目管理"菜单项 |
| `strings.xml` | Modify | 新增项目相关字符串 |
| `AndroidManifest.xml` | Modify | 注册 ProjectActivity |

---

## Task 1: Database Schema Migration

**Files:**
- Modify: `app/src/main/java/com/example/financialmanagement/database/DatabaseHelper.java`

- [ ] **Step 1: Add projects table constants and CREATE statement**

在 `DatabaseHelper.java` 中，在 `TABLE_EVENTS` 相关代码之后添加：

```java
public static final String TABLE_PROJECTS = "projects";
public static final String COLUMN_PROJECT_ID = "project_id";
public static final String COLUMN_PROJECT_NAME = "project_name";
public static final String COLUMN_PROJECT_CREATED_AT = "project_created_at";

private static final String CREATE_TABLE_PROJECTS =
        "CREATE TABLE " + TABLE_PROJECTS + " (" +
                COLUMN_PROJECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PROJECT_NAME + " TEXT NOT NULL UNIQUE, " +
                COLUMN_PROJECT_CREATED_AT + " INTEGER NOT NULL" +
                ")";
```

- [ ] **Step 2: Add project column to transactions**

在 `CREATE_TABLE_TRANSACTIONS` 的 `COLUMN_TIMESTAMP` 行之后添加：

```java
COLUMN_PROJECT + " TEXT NOT NULL DEFAULT '默认项目', " +
```

- [ ] **Step 3: Update DATABASE_VERSION to 5**

```java
public static final int DATABASE_VERSION = 5;
```

- [ ] **Step 4: Add migration in onUpgrade**

在 `onUpgrade()` 方法中，在 `oldVersion < 4` 块之后添加：

```java
if (oldVersion < 5) {
    db.execSQL(CREATE_TABLE_PROJECTS);
    // 插入默认项目
    android.content.ContentValues values = new android.content.ContentValues();
    values.put(COLUMN_PROJECT_NAME, "默认项目");
    values.put(COLUMN_PROJECT_CREATED_AT, System.currentTimeMillis());
    db.insert(TABLE_PROJECTS, null, values);
    // 为现有交易添加 project 列并设置默认值
    db.execSQL("ALTER TABLE " + TABLE_TRANSACTIONS + " ADD COLUMN " + COLUMN_PROJECT + " TEXT NOT NULL DEFAULT '默认项目'");
    // 更新所有现有交易的 project 为默认项目（默认值已经处理了，但为了确保）
    db.execSQL("UPDATE " + TABLE_TRANSACTIONS + " SET " + COLUMN_PROJECT + " = '默认项目'");
}
```

- [ ] **Step 5: Add onCreate insertion for default project**

在 `onCreate()` 中，在 `insertDefaultEvents(db)` 之后添加：

```java
// 插入默认项目
android.content.ContentValues projectValues = new android.content.ContentValues();
projectValues.put(COLUMN_PROJECT_NAME, "默认项目");
projectValues.put(COLUMN_PROJECT_CREATED_AT, System.currentTimeMillis());
db.insert(TABLE_PROJECTS, null, projectValues);
```

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/financialmanagement/database/DatabaseHelper.java
git commit -m "feat: add projects table and project column to transactions (db v5)"
```

---

## Task 2: Model Layer — Project.java + Transaction Update

**Files:**
- Create: `app/src/main/java/com/example/financialmanagement/model/Project.java`
- Modify: `app/src/main/java/com/example/financialmanagement/model/Transaction.java`

- [ ] **Step 1: Create Project.java**

```java
package com.example.financialmanagement.model;

import java.io.Serializable;

public class Project implements Serializable {
    private long id;
    private String name;
    private long createdAt;

    public Project() {
    }

    public Project(long id, String name, long createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
```

- [ ] **Step 2: Add project field to Transaction.java**

在 Transaction.java 中，添加字段、构造参数、getter/setter：

在类顶部添加：
```java
private String project;
```

在构造函数中初始化：
```java
public Transaction(long id, String type, double amount, String person, String event,
                   String date, String time, long timestamp, String project) {
    this.id = id;
    this.type = type;
    this.amount = amount;
    this.person = person;
    this.event = event;
    this.date = date;
    this.time = time;
    this.timestamp = timestamp;
    this.project = project;
}
```

添加 getter/setter：
```java
public String getProject() {
    return project;
}

public void setProject(String project) {
    this.project = project;
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/financialmanagement/model/Project.java
app/src/main/java/com/example/financialmanagement/model/Transaction.java
git commit -m "feat: add Project model and project field to Transaction"
```

---

## Task 3: DAO Layer — ProjectDao.java + TransactionDao Update

**Files:**
- Create: `app/src/main/java/com/example/financialmanagement/dao/ProjectDao.java`
- Modify: `app/src/main/java/com/example/financialmanagement/dao/TransactionDao.java`

- [ ] **Step 1: Create ProjectDao.java**

完全复用 PersonDao 模式，替换表名和列名：

```java
package com.example.financialmanagement.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.financialmanagement.database.DatabaseHelper;
import com.example.financialmanagement.model.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectDao {

    private final DatabaseHelper dbHelper;

    public ProjectDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PROJECT_NAME, name.trim());
        values.put(DatabaseHelper.COLUMN_PROJECT_CREATED_AT, System.currentTimeMillis());
        return db.insert(DatabaseHelper.TABLE_PROJECTS, null, values);
    }

    public int update(long id, String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PROJECT_NAME, name.trim());
        String whereClause = DatabaseHelper.COLUMN_PROJECT_ID + " = ?";
        String[] whereArgs = { String.valueOf(id) };
        return db.update(DatabaseHelper.TABLE_PROJECTS, values, whereClause, whereArgs);
    }

    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COLUMN_PROJECT_ID + " = ?";
        String[] whereArgs = { String.valueOf(id) };
        return db.delete(DatabaseHelper.TABLE_PROJECTS, whereClause, whereArgs);
    }

    public List<Project> getAll() {
        List<Project> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PROJECTS, null, null, null, null, null,
                DatabaseHelper.COLUMN_PROJECT_CREATED_AT + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToProject(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public boolean exists(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_PROJECT_NAME + " = ?";
        String[] selectionArgs = { name.trim() };
        Cursor cursor = db.query(DatabaseHelper.TABLE_PROJECTS, new String[]{ DatabaseHelper.COLUMN_PROJECT_ID },
                selection, selectionArgs, null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return exists;
    }

    public Project getByName(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_PROJECT_NAME + " = ?";
        String[] selectionArgs = { name.trim() };
        Cursor cursor = db.query(DatabaseHelper.TABLE_PROJECTS, null, selection, selectionArgs, null, null, null);
        Project project = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                project = cursorToProject(cursor);
            }
            cursor.close();
        }
        return project;
    }

    private Project cursorToProject(Cursor cursor) {
        Project p = new Project();
        p.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_ID)));
        p.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_NAME)));
        p.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_CREATED_AT)));
        return p;
    }
}
```

- [ ] **Step 2: Update TransactionDao to filter by project**

TransactionDao.java 需要修改以下方法：

**方法签名变更：** 所有公共查询/统计方法新增 `String project` 参数。

以 `getAll()` 为例，改为 `getAllByProject(String project)`：

```java
public List<Transaction> getAllByProject(String project) {
    List<Transaction> list = new ArrayList<>();
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    String selection = DatabaseHelper.COLUMN_PROJECT + " = ?";
    String[] selectionArgs = { project };
    Cursor cursor = db.query(DatabaseHelper.TABLE_TRANSACTIONS, null, selection, selectionArgs, null, null,
            DatabaseHelper.COLUMN_TIMESTAMP + " DESC");
    if (cursor != null && cursor.moveToFirst()) {
        do {
            list.add(cursorToTransaction(cursor));
        } while (cursor.moveToNext());
        cursor.close();
    }
    return list;
}
```

类似地，修改所有方法：
- `getByDate(String date)` → `getByDateAndProject(String date, String project)`
- `getByYearMonth(String yearMonth)` → `getByYearMonthAndProject(String yearMonth, String project)`
- `getByDateRange(...)` → `getByDateRangeAndProject(..., String project)`
- `getByYear(String year)` → `getByYearAndProject(String year, String project)`
- `getByPerson(String person)` → `getByPersonAndProject(String person, String project)`
- `getTotalIncome()` → `getTotalIncomeByProject(String project)`
- `getTotalExpense()` → `getTotalExpenseByProject(String project)`
- `getDailyTrend(...)` → `getDailyTrendByProject(..., String project)`
- 以及所有带日期/人员过滤的 total 方法

**insert() 方法更新：**
```java
public long insert(Transaction transaction) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(DatabaseHelper.COLUMN_TYPE, transaction.getType());
    values.put(DatabaseHelper.COLUMN_AMOUNT, transaction.getAmount());
    values.put(DatabaseHelper.COLUMN_PERSON, transaction.getPerson());
    values.put(DatabaseHelper.COLUMN_EVENT, transaction.getEvent());
    values.put(DatabaseHelper.COLUMN_DATE, transaction.getDate());
    values.put(DatabaseHelper.COLUMN_TIME, transaction.getTime());
    values.put(DatabaseHelper.COLUMN_TIMESTAMP, transaction.getTimestamp());
    values.put(DatabaseHelper.COLUMN_PROJECT, transaction.getProject());
    return db.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, values);
}
```

**update() 方法更新：** 同样添加 `COLUMN_PROJECT` 到 ContentValues。

**cursorToTransaction() 更新：**
```java
private Transaction cursorToTransaction(Cursor cursor) {
    Transaction t = new Transaction();
    t.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
    t.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE)));
    t.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT)));
    t.setPerson(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PERSON)));
    t.setEvent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT)));
    t.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE)));
    t.setTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME)));
    t.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIMESTAMP)));
    t.setProject(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT)));
    return t;
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/financialmanagement/dao/ProjectDao.java
app/src/main/java/com/example/financialmanagement/dao/TransactionDao.java
git commit -m "feat: add ProjectDao and update TransactionDao with project filtering"
```

---

## Task 4: UI Layout Resources

**Files:**
- Modify: `app/src/main/res/layout/activity_main.xml`
- Create: `app/src/main/res/layout/activity_project.xml`
- Create: `app/src/main/res/layout/item_project.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/menu/menu_main.xml`

- [ ] **Step 1: Update activity_main.xml — Replace Toolbar title with Spinner**

将 Toolbar 从：
```xml
<com.google.android.material.appbar.MaterialToolbar
    android:id="@+id/toolbar"
    android:layout_width="match_parent"
    android:layout_height="?attr/actionBarSize"
    android:background="@color/primary"
    app:title="@string/app_name"
    app:titleTextColor="@color/white"
    app:titleTextAppearance="@style/TextAppearance.Financial.Toolbar" />
```

改为 Toolbar 内嵌 Spinner：
```xml
<com.google.android.material.appbar.MaterialToolbar
    android:id="@+id/toolbar"
    android:layout_width="match_parent"
    android:layout_height="?attr/actionBarSize"
    android:background="@color/primary">

    <Spinner
        android:id="@+id/spinner_project"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:backgroundTint="@color/white"
        android:textColor="@color/white" />
</com.google.android.material.appbar.MaterialToolbar>
```

- [ ] **Step 2: Create activity_project.xml**

复用 activity_person.xml 结构（去掉头像相关）：

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/light_gray">

    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="@color/primary"
        app:title="@string/manage_projects"
        app:titleTextColor="@color/white" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="16dp">

        <EditText
            android:id="@+id/et_project_name"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:hint="@string/hint_project_name"
            android:textColor="@color/on_surface"
            android:textColorHint="@color/gray"
            android:background="@drawable/bg_input"
            android:padding="12dp" />

        <Button
            android:id="@+id/btn_add_project"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:text="@string/add"
            android:backgroundTint="@color/accent"
            android:textColor="@color/white" />
    </LinearLayout>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recycler_view"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:padding="12dp"
        android:clipToPadding="false" />

    <TextView
        android:id="@+id/tv_empty"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:gravity="center"
        android:text="@string/no_projects"
        android:textSize="16sp"
        android:textColor="@color/gray"
        android:visibility="gone" />
</LinearLayout>
```

- [ ] **Step 3: Create item_project.xml**

复用 item_person.xml 结构（去掉头像）：

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:padding="12dp"
    android:background="@drawable/bg_card"
    android:layout_margin="4dp">

    <TextView
        android:id="@+id/tv_name"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:textSize="16sp"
        android:textColor="@color/on_surface" />

    <ImageButton
        android:id="@+id/btn_edit"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:src="@android:drawable/ic_menu_edit"
        android:background="?attr/selectableItemBackgroundBorderless"
        android:contentDescription="@string/edit_record" />

    <ImageButton
        android:id="@+id/btn_delete"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:src="@android:drawable/ic_menu_delete"
        android:background="?attr/selectableItemBackgroundBorderless"
        android:contentDescription="@string/delete" />
</LinearLayout>
```

- [ ] **Step 4: Add strings to strings.xml**

在 `</resources>` 之前添加：

```xml
<string name="manage_projects">项目管理</string>
<string name="hint_project_name">请输入项目名称</string>
<string name="no_projects">暂无项目</string>
<string name="project_exists">该项目已存在</string>
<string name="project_added">项目已添加</string>
<string name="project_updated">项目已更新</string>
<string name="project_deleted">项目已删除</string>
<string name="cannot_delete_current_project">无法删除当前使用的项目</string>
```

- [ ] **Step 5: Add menu item to menu_main.xml**

在 `action_export_json` 之后添加：

```xml
<item
    android:id="@+id/action_manage_projects"
    android:title="@string/manage_projects"
    android:orderInCategory="103"
    app:showAsAction="never" />
```

- [ ] **Step 6: Commit**

```bash
git add app/src/main/res/layout/activity_main.xml
app/src/main/res/layout/activity_project.xml
app/src/main/res/layout/item_project.xml
app/src/main/res/values/strings.xml
app/src/main/res/menu/menu_main.xml
git commit -m "feat: add project UI layouts and menu resources"
```

---

## Task 5: MainActivity — Project Spinner + SharedPreferences

**Files:**
- Modify: `app/src/main/java/com/example/financialmanagement/MainActivity.java`

- [ ] **Step 1: Add imports**

```java
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
```

- [ ] **Step 2: Add constants and fields**

```java
private static final String PREFS_NAME = "app_prefs";
private static final String KEY_CURRENT_PROJECT = "current_project";

private Spinner spinnerProject;
private ProjectDao projectDao;
private String currentProject;
```

- [ ] **Step 3: Initialize Spinner and load current project in onCreate**

在 `onCreate()` 中，在 `transactionDao = new TransactionDao(this)` 之后添加：

```java
projectDao = new ProjectDao(this);

// 读取上次使用的项目
SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
currentProject = sp.getString(KEY_CURRENT_PROJECT, "默认项目");

// 初始化 Spinner
spinnerProject = findViewById(R.id.spinner_project);
setupProjectSpinner();
```

- [ ] **Step 4: Add setupProjectSpinner() method**

```java
private void setupProjectSpinner() {
    List<Project> projects = projectDao.getAll();
    List<String> projectNames = new ArrayList<>();
    for (Project p : projects) {
        projectNames.add(p.getName());
    }

    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, projectNames);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinnerProject.setAdapter(adapter);

    // 选中当前项目
    int position = projectNames.indexOf(currentProject);
    if (position >= 0) {
        spinnerProject.setSelection(position);
    }

    spinnerProject.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
            String selected = projectNames.get(pos);
            if (!selected.equals(currentProject)) {
                currentProject = selected;
                SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                sp.edit().putString(KEY_CURRENT_PROJECT, currentProject).apply();
                loadData();
            }
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
        }
    });
}
```

- [ ] **Step 5: Update loadData() to filter by project**

将 `loadData()` 中的：
```java
List<Transaction> list = transactionDao.getAll();
```

改为：
```java
List<Transaction> list = transactionDao.getAllByProject(currentProject);
```

同样更新统计数据的获取（`getTotalIncome()` → `getTotalIncomeByProject(currentProject)` 等）。

注意：需要确认 TransactionDao 中对应的方法已经更新（在 Task 3 中完成）。

- [ ] **Step 6: Update onResume() to refresh spinner**

```java
@Override
protected void onResume() {
    super.onResume();
    setupProjectSpinner(); // 刷新项目列表（可能新增了项目）
    loadData();
}
```

- [ ] **Step 7: Add menu handler for project management**

在 `onOptionsItemSelected()` 中添加：

```java
} else if (id == R.id.action_manage_projects) {
    openProjects();
    return true;
}
```

- [ ] **Step 8: Add openProjects() method**

```java
private void openProjects() {
    Intent intent = new Intent(this, ProjectActivity.class);
    startActivity(intent);
}
```

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/example/financialmanagement/MainActivity.java
git commit -m "feat: add project spinner and SharedPreferences to MainActivity"
```

---

## Task 6: ProjectAdapter

**Files:**
- Create: `app/src/main/java/com/example/financialmanagement/adapter/ProjectAdapter.java`

- [ ] **Step 1: Create ProjectAdapter.java**

复用 PersonAdapter 模式，去掉头像逻辑：

```java
package com.example.financialmanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financialmanagement.R;
import com.example.financialmanagement.model.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    private List<Project> projects = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(Project project);
        void onDeleteClick(Project project);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects != null ? projects : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Project project = projects.get(position);
        holder.tvName.setText(project.getName());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(project);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(project);
        });
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/financialmanagement/adapter/ProjectAdapter.java
git commit -m "feat: add ProjectAdapter for RecyclerView"
```

---

## Task 7: ProjectActivity

**Files:**
- Create: `app/src/main/java/com/example/financialmanagement/ProjectActivity.java`

- [ ] **Step 1: Create ProjectActivity.java**

复用 PersonActivity 模式，去掉头像相关逻辑：

```java
package com.example.financialmanagement;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financialmanagement.adapter.ProjectAdapter;
import com.example.financialmanagement.dao.ProjectDao;
import com.example.financialmanagement.model.Project;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class ProjectActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_CURRENT_PROJECT = "current_project";

    private RecyclerView recyclerView;
    private View tvEmpty;
    private EditText etProjectName;
    private ProjectAdapter adapter;
    private ProjectDao projectDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        projectDao = new ProjectDao(this);

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);
        etProjectName = findViewById(R.id.et_project_name);

        adapter = new ProjectAdapter();
        adapter.setOnItemClickListener(new ProjectAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Project project) {
                showEditDialog(project);
            }

            @Override
            public void onDeleteClick(Project project) {
                showDeleteDialog(project);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_add_project).setOnClickListener(v -> addProject());

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<Project> list = projectDao.getAll();
        adapter.setProjects(list);

        if (list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void addProject() {
        String name = etProjectName.getText().toString().trim();
        if (name.isEmpty()) {
            etProjectName.setError("请输入项目名称");
            return;
        }
        if (projectDao.exists(name)) {
            Toast.makeText(this, R.string.project_exists, Toast.LENGTH_SHORT).show();
            return;
        }
        projectDao.insert(name);
        etProjectName.setText("");
        loadData();
        Toast.makeText(this, R.string.project_added, Toast.LENGTH_SHORT).show();
    }

    private void showEditDialog(Project project) {
        final EditText input = new EditText(this);
        input.setText(project.getName());
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_record)
                .setView(input)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = input.getText().toString().trim();
                        if (newName.isEmpty()) {
                            Toast.makeText(ProjectActivity.this, "项目名称不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!newName.equals(project.getName()) && projectDao.exists(newName)) {
                            Toast.makeText(ProjectActivity.this, R.string.project_exists, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        projectDao.update(project.getId(), newName);
                        loadData();
                        Toast.makeText(ProjectActivity.this, R.string.project_updated, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDeleteDialog(Project project) {
        // 检查是否是当前使用的项目
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentProject = sp.getString(KEY_CURRENT_PROJECT, "默认项目");
        if (project.getName().equals(currentProject)) {
            Toast.makeText(this, R.string.cannot_delete_current_project, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage("确定要删除 " + project.getName() + " 吗？")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        projectDao.delete(project.getId());
                        loadData();
                        Toast.makeText(ProjectActivity.this, R.string.project_deleted, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/financialmanagement/ProjectActivity.java
git commit -m "feat: add ProjectActivity for project CRUD management"
```

---

## Task 8: AddEditActivity — Write Project to Transaction

**Files:**
- Modify: `app/src/main/java/com/example/financialmanagement/AddEditActivity.java`

- [ ] **Step 1: Read current project and set on transaction**

在 `saveTransaction()` 方法中，在 `transaction.setTimestamp(...)` 之后添加：

```java
// 读取当前项目
SharedPreferences sp = getSharedPreferences("app_prefs", MODE_PRIVATE);
String currentProject = sp.getString("current_project", "默认项目");
transaction.setProject(currentProject);
```

- [ ] **Step 2: Add import**

```java
import android.content.SharedPreferences;
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/financialmanagement/AddEditActivity.java
git commit -m "feat: save current project when creating/editing transaction"
```

---

## Task 9: AndroidManifest + StatisticsActivity/ChartActivity Update

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/example/financialmanagement/StatisticsActivity.java`
- Modify: `app/src/main/java/com/example/financialmanagement/ChartActivity.java`

- [ ] **Step 1: Register ProjectActivity in AndroidManifest.xml**

在 `</application>` 之前添加：

```xml
<activity
    android:name=".ProjectActivity"
    android:exported="false" />
```

- [ ] **Step 2: Update StatisticsActivity to filter by project**

StatisticsActivity 需要：
1. 读取当前项目 SharedPreferences
2. 所有 TransactionDao 调用传入 project 参数
3. `loadPersonSpinner()` 保持不变（人员全局共享）

在类中添加字段：
```java
private String currentProject;
```

在 `onCreate()` 中初始化：
```java
SharedPreferences sp = getSharedPreferences("app_prefs", MODE_PRIVATE);
currentProject = sp.getString("current_project", "默认项目");
```

更新 `updateStatistics()` 中所有 DAO 调用为带 project 参数的版本。

- [ ] **Step 3: Update ChartActivity to filter by project**

ChartActivity 同样需要：
1. 读取当前项目
2. `getDailyTrend()` 调用传入 project 参数

在类中添加字段并在 `onCreate()` 中读取。

- [ ] **Step 4: Commit**

```bash
git add app/src/main/AndroidManifest.xml
app/src/main/java/com/example/financialmanagement/StatisticsActivity.java
app/src/main/java/com/example/financialmanagement/ChartActivity.java
git commit -m "feat: register ProjectActivity and update Stats/Chart with project filter"
```

---

## Task 10: Compile Verification

- [ ] **Step 1: Compile the project**

```bash
gradle compileDebugJavaWithJavac
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: If compilation fails, fix errors**

常见错误及修复：
- 方法签名不匹配 → 检查 TransactionDao 中所有方法名与调用处一致
- 缺少 import → 添加对应 import
- R.id 找不到 → 确认布局文件已保存且 ID 正确

- [ ] **Step 3: Commit final changes**

```bash
git add .
git commit -m "fix: compilation fixes for project isolation feature"
```

---

## Self-Review Checklist

- [ ] **Spec coverage:** 所有设计要求都有对应的 Task
- [ ] **Placeholder scan:** 无 TBD/TODO/"implement later"
- [ ] **Type consistency:** TransactionDao 方法签名在 Task 3 和调用处（Task 5/9）一致
- [ ] **File paths:** 所有路径使用实际项目路径
- [ ] **Commit granularity:** 每 Task 至少一个 commit
