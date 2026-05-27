package com.example.financialmanagement.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "financial_management.db";
    public static final int DATABASE_VERSION = 5;

    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_PERSON = "person";
    public static final String COLUMN_EVENT = "event";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String CREATE_TABLE_TRANSACTIONS =
            "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TYPE + " TEXT NOT NULL, " +
                    COLUMN_AMOUNT + " REAL NOT NULL, " +
                    COLUMN_PERSON + " TEXT NOT NULL, " +
                    COLUMN_EVENT + " TEXT NOT NULL, " +
                    COLUMN_DATE + " TEXT NOT NULL, " +
                    COLUMN_TIME + " TEXT NOT NULL, " +
                    COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                    "project TEXT NOT NULL DEFAULT '默认项目'" +
                    ")";

    public static final String TABLE_PERSONS = "persons";
    public static final String COLUMN_PERSON_ID = "person_id";
    public static final String COLUMN_PERSON_NAME = "person_name";
    public static final String COLUMN_PERSON_CREATED_AT = "created_at";
    public static final String COLUMN_PERSON_AVATAR = "avatar";

    private static final String CREATE_TABLE_PERSONS =
            "CREATE TABLE " + TABLE_PERSONS + " (" +
                    COLUMN_PERSON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PERSON_NAME + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_PERSON_AVATAR + " TEXT, " +
                    COLUMN_PERSON_CREATED_AT + " INTEGER NOT NULL" +
                    ")";

    public static final String TABLE_EVENTS = "events";
    public static final String COLUMN_EVENT_ID = "event_id";
    public static final String COLUMN_EVENT_NAME = "event_name";
    public static final String COLUMN_EVENT_CREATED_AT = "event_created_at";

    private static final String CREATE_TABLE_EVENTS =
            "CREATE TABLE " + TABLE_EVENTS + " (" +
                    COLUMN_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_EVENT_NAME + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_EVENT_CREATED_AT + " INTEGER NOT NULL" +
                    ")";

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

    public static final String COLUMN_PROJECT = "project";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * 关闭数据库连接（恢复前必须调用，防止文件被占用）
     */
    public static synchronized void closeDatabase() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    /**
     * 重置单例实例（恢复后重新初始化）
     */
    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_PERSONS);
        db.execSQL(CREATE_TABLE_EVENTS);
        db.execSQL(CREATE_TABLE_PROJECTS);
        insertDefaultEvents(db);
        insertDefaultProject(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(CREATE_TABLE_PERSONS);
        }
        if (oldVersion < 3) {
            db.execSQL(CREATE_TABLE_EVENTS);
            insertDefaultEvents(db);
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_PERSONS + " ADD COLUMN " + COLUMN_PERSON_AVATAR + " TEXT");
        }
        if (oldVersion < 5) {
            db.execSQL(CREATE_TABLE_PROJECTS);
            insertDefaultProject(db);
            db.execSQL("ALTER TABLE " + TABLE_TRANSACTIONS + " ADD COLUMN " + COLUMN_PROJECT + " TEXT NOT NULL DEFAULT '默认项目'");
            db.execSQL("UPDATE " + TABLE_TRANSACTIONS + " SET " + COLUMN_PROJECT + " = '默认项目'");
        }
    }

    private void insertDefaultEvents(SQLiteDatabase db) {
        String[] defaults = { "餐饮", "交通", "购物", "娱乐", "工资", "奖金", "投资", "其他" };
        long now = System.currentTimeMillis();
        for (String name : defaults) {
            try {
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(COLUMN_EVENT_NAME, name);
                values.put(COLUMN_EVENT_CREATED_AT, now);
                db.insertOrThrow(TABLE_EVENTS, null, values);
            } catch (Exception e) {
                // ignore duplicate
            }
        }
    }

    private void insertDefaultProject(SQLiteDatabase db) {
        long now = System.currentTimeMillis();
        try {
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(COLUMN_PROJECT_NAME, "默认项目");
            values.put(COLUMN_PROJECT_CREATED_AT, now);
            db.insertOrThrow(TABLE_PROJECTS, null, values);
        } catch (Exception e) {
            // ignore duplicate
        }
    }
}
