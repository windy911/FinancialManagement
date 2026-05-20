package com.example.financialmanagement.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.financialmanagement.database.DatabaseHelper;
import com.example.financialmanagement.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventDao {

    private final DatabaseHelper dbHelper;

    public EventDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_EVENT_NAME, name.trim());
        values.put(DatabaseHelper.COLUMN_EVENT_CREATED_AT, System.currentTimeMillis());
        return db.insert(DatabaseHelper.TABLE_EVENTS, null, values);
    }

    public int update(long id, String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_EVENT_NAME, name.trim());
        String whereClause = DatabaseHelper.COLUMN_EVENT_ID + " = ?";
        String[] whereArgs = { String.valueOf(id) };
        return db.update(DatabaseHelper.TABLE_EVENTS, values, whereClause, whereArgs);
    }

    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COLUMN_EVENT_ID + " = ?";
        String[] whereArgs = { String.valueOf(id) };
        return db.delete(DatabaseHelper.TABLE_EVENTS, whereClause, whereArgs);
    }

    public List<Event> getAll() {
        List<Event> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_EVENTS, null, null, null, null, null,
                DatabaseHelper.COLUMN_EVENT_CREATED_AT + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToEvent(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public boolean exists(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_EVENT_NAME + " = ?";
        String[] selectionArgs = { name.trim() };
        Cursor cursor = db.query(DatabaseHelper.TABLE_EVENTS, new String[]{ DatabaseHelper.COLUMN_EVENT_ID },
                selection, selectionArgs, null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return exists;
    }

    private Event cursorToEvent(Cursor cursor) {
        Event e = new Event();
        e.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_ID)));
        e.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_NAME)));
        e.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_CREATED_AT)));
        return e;
    }
}
