package com.example.financialmanagement.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.financialmanagement.database.DatabaseHelper;
import com.example.financialmanagement.model.Person;

import java.util.ArrayList;
import java.util.List;

public class PersonDao {

    private final DatabaseHelper dbHelper;

    public PersonDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PERSON_NAME, name.trim());
        values.put(DatabaseHelper.COLUMN_PERSON_CREATED_AT, System.currentTimeMillis());
        return db.insert(DatabaseHelper.TABLE_PERSONS, null, values);
    }

    public int update(long id, String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PERSON_NAME, name.trim());
        String whereClause = DatabaseHelper.COLUMN_PERSON_ID + " = ?";
        String[] whereArgs = { String.valueOf(id) };
        return db.update(DatabaseHelper.TABLE_PERSONS, values, whereClause, whereArgs);
    }

    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COLUMN_PERSON_ID + " = ?";
        String[] whereArgs = { String.valueOf(id) };
        return db.delete(DatabaseHelper.TABLE_PERSONS, whereClause, whereArgs);
    }

    public List<Person> getAll() {
        List<Person> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PERSONS, null, null, null, null, null,
                DatabaseHelper.COLUMN_PERSON_CREATED_AT + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToPerson(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public boolean exists(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_PERSON_NAME + " = ?";
        String[] selectionArgs = { name.trim() };
        Cursor cursor = db.query(DatabaseHelper.TABLE_PERSONS, new String[]{ DatabaseHelper.COLUMN_PERSON_ID },
                selection, selectionArgs, null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return exists;
    }

    private Person cursorToPerson(Cursor cursor) {
        Person p = new Person();
        p.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PERSON_ID)));
        p.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PERSON_NAME)));
        p.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PERSON_CREATED_AT)));
        return p;
    }
}
