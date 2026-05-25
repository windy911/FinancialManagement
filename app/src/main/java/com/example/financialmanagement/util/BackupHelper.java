package com.example.financialmanagement.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.example.financialmanagement.database.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 单机数据备份与恢复工具类
 * 使用 Storage Access Framework (SAF) 进行备份/恢复，无需额外权限
 * 支持 JSON 格式导出（跨平台可读）和 SQLite 原文件备份（完整恢复）
 */
public class BackupHelper {

    private static final String BACKUP_PREFIX = "FinancialBackup_";
    private static final String BACKUP_SUFFIX = ".db";
    private static final String JSON_SUFFIX = ".json";

    /** 备份结果回调 */
    public interface BackupCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    /**
     * 生成默认备份文件名：FinancialBackup_20260520_143052.db
     */
    public static String generateBackupFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return BACKUP_PREFIX + sdf.format(new Date()) + BACKUP_SUFFIX;
    }

    /**
     * 获取数据库文件对象
     */
    public static File getDatabaseFile(Context context) {
        return context.getDatabasePath(DatabaseHelper.DATABASE_NAME);
    }

    /**
     * 执行备份：将 SQLite 数据库文件复制到用户指定的 URI
     *
     * @param context  上下文
     * @param destUri  用户通过 SAF 选择的目标文件 URI
     * @param callback 结果回调
     */
    public static void performBackup(Context context, Uri destUri, BackupCallback callback) {
        File dbFile = getDatabaseFile(context);
        if (!dbFile.exists()) {
            callback.onError("数据库文件不存在，暂无数据可备份");
            return;
        }

        SQLiteDatabase db = null;
        try {
            // 先做一次 WAL checkpoint，确保所有数据已写入主文件
            db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null,
                    SQLiteDatabase.OPEN_READONLY);
            db.rawQuery("PRAGMA wal_checkpoint(TRUNCATE)", null).close();

            try (ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(destUri, "w");
                 FileChannel srcChannel = new FileInputStream(dbFile).getChannel();
                 FileChannel dstChannel = new FileOutputStream(pfd.getFileDescriptor()).getChannel()) {
                srcChannel.transferTo(0, srcChannel.size(), dstChannel);
            }

            callback.onSuccess("备份成功！\n文件已保存至所选位置");
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError("备份失败：" + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * 执行恢复：从用户选择的备份 URI 恢复数据库
     *
     * @param context  上下文
     * @param srcUri   用户通过 SAF 选择的备份文件 URI
     * @param callback 结果回调
     */
    public static void performRestore(Context context, Uri srcUri, BackupCallback callback) {
        File dbFile = getDatabaseFile(context);

        // 1. 验证备份文件
        if (!validateBackupFile(context, srcUri, callback)) {
            return;
        }

        // 2. 关闭所有数据库连接（关键步骤）
        DatabaseHelper.closeDatabase();

        // 3. 复制备份文件覆盖现有数据库
        try (InputStream is = context.getContentResolver().openInputStream(srcUri);
             FileOutputStream fos = new FileOutputStream(dbFile)) {
            if (is == null) {
                callback.onError("无法读取备份文件");
                return;
            }
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.getFD().sync(); // 确保写入磁盘

            // 4. 重置 DatabaseHelper 单例，重新打开验证
            DatabaseHelper.resetInstance();
            DatabaseHelper helper = DatabaseHelper.getInstance(context);
            SQLiteDatabase db = helper.getReadableDatabase();
            db.rawQuery("PRAGMA integrity_check", null).close();
            db.close();

            callback.onSuccess("恢复成功！应用将重新加载数据");
        } catch (Exception e) {
            e.printStackTrace();
            DatabaseHelper.resetInstance(); // 确保即使失败也能重新初始化
            callback.onError("恢复失败：" + e.getMessage());
        }
    }

    /**
     * 验证备份文件是否为本应用的有效数据库
     */
    private static boolean validateBackupFile(Context context, Uri uri, BackupCallback callback) {
        try (ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r")) {
            if (pfd == null) {
                callback.onError("无法打开备份文件");
                return false;
            }

            // 使用临时文件进行验证（SQLite 需要文件路径）
            File tempFile = File.createTempFile("backup_verify", ".db", context.getCacheDir());
            try (FileChannel src = new FileInputStream(pfd.getFileDescriptor()).getChannel();
                 FileChannel dst = new FileOutputStream(tempFile).getChannel()) {
                src.transferTo(0, src.size(), dst);
            }

            SQLiteDatabase db = null;
            try {
                db = SQLiteDatabase.openDatabase(tempFile.getAbsolutePath(), null,
                        SQLiteDatabase.OPEN_READONLY);
                // 检查关键表是否存在
                boolean hasTransactions = tableExists(db, DatabaseHelper.TABLE_TRANSACTIONS);
                boolean hasPersons = tableExists(db, DatabaseHelper.TABLE_PERSONS);
                boolean hasEvents = tableExists(db, DatabaseHelper.TABLE_EVENTS);

                if (!hasTransactions || !hasPersons || !hasEvents) {
                    callback.onError("备份文件格式不正确，缺少必要的数据表");
                    return false;
                }

                // 完整性检查
                android.database.Cursor cursor = db.rawQuery("PRAGMA integrity_check", null);
                String result = "";
                if (cursor.moveToFirst()) {
                    result = cursor.getString(0);
                }
                cursor.close();

                if (!"ok".equalsIgnoreCase(result)) {
                    callback.onError("备份文件已损坏，完整性检查未通过");
                    return false;
                }

                return true;
            } finally {
                if (db != null && db.isOpen()) {
                    db.close();
                }
                tempFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError("备份文件验证失败：" + e.getMessage());
            return false;
        }
    }

    private static boolean tableExists(SQLiteDatabase db, String tableName) {
        android.database.Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * 导出为 JSON 格式（便于跨平台查看和编辑）
     */
    public static void exportToJson(Context context, Uri destUri, BackupCallback callback) {
        DatabaseHelper helper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        try {
            JSONObject root = new JSONObject();
            root.put("app_name", "FinancialManagement");
            root.put("export_time", System.currentTimeMillis());
            root.put("version", DatabaseHelper.DATABASE_VERSION);

            // 导出 transactions
            JSONArray transactions = new JSONArray();
            android.database.Cursor c = db.query(DatabaseHelper.TABLE_TRANSACTIONS, null, null, null, null, null,
                    DatabaseHelper.COLUMN_TIMESTAMP + " DESC");
            while (c.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("id", c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
                obj.put("type", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE)));
                obj.put("amount", c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT)));
                obj.put("person", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PERSON)));
                obj.put("event", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT)));
                obj.put("date", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE)));
                obj.put("time", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME)));
                obj.put("timestamp", c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIMESTAMP)));
                transactions.put(obj);
            }
            c.close();
            root.put("transactions", transactions);

            // 导出 persons
            JSONArray persons = new JSONArray();
            c = db.query(DatabaseHelper.TABLE_PERSONS, null, null, null, null, null,
                    DatabaseHelper.COLUMN_PERSON_CREATED_AT + " DESC");
            while (c.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("id", c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PERSON_ID)));
                obj.put("name", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PERSON_NAME)));
                obj.put("avatar", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PERSON_AVATAR)));
                obj.put("created_at", c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PERSON_CREATED_AT)));
                persons.put(obj);
            }
            c.close();
            root.put("persons", persons);

            // 导出 events
            JSONArray events = new JSONArray();
            c = db.query(DatabaseHelper.TABLE_EVENTS, null, null, null, null, null,
                    DatabaseHelper.COLUMN_EVENT_CREATED_AT + " DESC");
            while (c.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("id", c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_ID)));
                obj.put("name", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_NAME)));
                obj.put("created_at", c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_CREATED_AT)));
                events.put(obj);
            }
            c.close();
            root.put("events", events);

            try (OutputStream os = context.getContentResolver().openOutputStream(destUri)) {
                if (os != null) {
                    os.write(root.toString(2).getBytes("UTF-8"));
                }
            }

            callback.onSuccess("JSON 导出成功！\n共导出 " + transactions.length() + " 条记录");
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError("导出失败：" + e.getMessage());
        }
    }
}
