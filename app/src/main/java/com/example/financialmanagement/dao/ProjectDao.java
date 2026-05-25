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
