package com.example.financialmanagement.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.financialmanagement.database.DatabaseHelper;
import com.example.financialmanagement.model.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionDao {

    private final DatabaseHelper dbHelper;

    public TransactionDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

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
        return db.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, values);
    }

    public int update(Transaction transaction) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TYPE, transaction.getType());
        values.put(DatabaseHelper.COLUMN_AMOUNT, transaction.getAmount());
        values.put(DatabaseHelper.COLUMN_PERSON, transaction.getPerson());
        values.put(DatabaseHelper.COLUMN_EVENT, transaction.getEvent());
        values.put(DatabaseHelper.COLUMN_DATE, transaction.getDate());
        values.put(DatabaseHelper.COLUMN_TIME, transaction.getTime());
        values.put(DatabaseHelper.COLUMN_TIMESTAMP, transaction.getTimestamp());
        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = { String.valueOf(transaction.getId()) };
        return db.update(DatabaseHelper.TABLE_TRANSACTIONS, values, whereClause, whereArgs);
    }

    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = { String.valueOf(id) };
        return db.delete(DatabaseHelper.TABLE_TRANSACTIONS, whereClause, whereArgs);
    }

    public Transaction getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        Cursor cursor = db.query(DatabaseHelper.TABLE_TRANSACTIONS, null, selection, selectionArgs, null, null, null);
        Transaction transaction = null;
        if (cursor != null && cursor.moveToFirst()) {
            transaction = cursorToTransaction(cursor);
            cursor.close();
        }
        return transaction;
    }

    public List<Transaction> getAll() {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_TRANSACTIONS, null, null, null, null, null,
                DatabaseHelper.COLUMN_TIMESTAMP + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToTransaction(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Transaction> getByDate(String date) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_DATE + " = ?";
        String[] selectionArgs = { date };
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

    public List<Transaction> getByYearMonth(String yearMonth) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_DATE + " LIKE ?";
        String[] selectionArgs = { yearMonth + "%" };
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

    public List<Transaction> getByDateRange(String startDate, String endDate) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_DATE + " >= ? AND " + DatabaseHelper.COLUMN_DATE + " <= ?";
        String[] selectionArgs = { startDate, endDate };
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

    public List<Transaction> getByYear(String year) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_DATE + " LIKE ?";
        String[] selectionArgs = { year + "%" };
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

    public double getTotalIncome() {
        return getTotalByType(Transaction.TYPE_INCOME);
    }

    public double getTotalExpense() {
        return getTotalByType(Transaction.TYPE_EXPENSE);
    }

    public double getTotalIncomeByDate(String date) {
        return getTotalByTypeAndDate(Transaction.TYPE_INCOME, date);
    }

    public double getTotalExpenseByDate(String date) {
        return getTotalByTypeAndDate(Transaction.TYPE_EXPENSE, date);
    }

    public double getTotalIncomeByYearMonth(String yearMonth) {
        return getTotalByTypeAndYearMonth(Transaction.TYPE_INCOME, yearMonth);
    }

    public double getTotalExpenseByYearMonth(String yearMonth) {
        return getTotalByTypeAndYearMonth(Transaction.TYPE_EXPENSE, yearMonth);
    }

    public double getTotalIncomeByYear(String year) {
        return getTotalByTypeAndYear(Transaction.TYPE_INCOME, year);
    }

    public double getTotalExpenseByYear(String year) {
        return getTotalByTypeAndYear(Transaction.TYPE_EXPENSE, year);
    }

    public double getTotalIncomeByDateRange(String startDate, String endDate) {
        return getTotalByTypeAndDateRange(Transaction.TYPE_INCOME, startDate, endDate);
    }

    public double getTotalExpenseByDateRange(String startDate, String endDate) {
        return getTotalByTypeAndDateRange(Transaction.TYPE_EXPENSE, startDate, endDate);
    }

    public List<Transaction> getByPerson(String person) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_PERSON + " = ?";
        String[] selectionArgs = { person };
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

    public double getTotalIncomeByPerson(String person) {
        return getTotalByTypeAndPerson(Transaction.TYPE_INCOME, person);
    }

    public double getTotalExpenseByPerson(String person) {
        return getTotalByTypeAndPerson(Transaction.TYPE_EXPENSE, person);
    }

    private double getTotalByTypeAndPerson(String type, String person) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TYPE + " = ? AND " + DatabaseHelper.COLUMN_PERSON + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{ type, person });
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    private double getTotalByType(String type) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TYPE + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{ type });
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    private double getTotalByTypeAndDate(String type, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TYPE + " = ? AND " + DatabaseHelper.COLUMN_DATE + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{ type, date });
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    private double getTotalByTypeAndYearMonth(String type, String yearMonth) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TYPE + " = ? AND " + DatabaseHelper.COLUMN_DATE + " LIKE ?";
        Cursor cursor = db.rawQuery(sql, new String[]{ type, yearMonth + "%" });
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    private double getTotalByTypeAndDateRange(String type, String startDate, String endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TYPE + " = ? AND " +
                DatabaseHelper.COLUMN_DATE + " >= ? AND " + DatabaseHelper.COLUMN_DATE + " <= ?";
        Cursor cursor = db.rawQuery(sql, new String[]{ type, startDate, endDate });
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    private double getTotalByTypeAndYear(String type, String year) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TYPE + " = ? AND " + DatabaseHelper.COLUMN_DATE + " LIKE ?";
        Cursor cursor = db.rawQuery(sql, new String[]{ type, year + "%" });
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    public java.util.Map<String, double[]> getDailyTrend(String startDate, String endDate) {
        java.util.Map<String, double[]> map = new java.util.TreeMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT " + DatabaseHelper.COLUMN_DATE + ", " + DatabaseHelper.COLUMN_TYPE +
                ", SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") " +
                "FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_DATE + " >= ? AND " + DatabaseHelper.COLUMN_DATE + " <= ?" +
                " GROUP BY " + DatabaseHelper.COLUMN_DATE + ", " + DatabaseHelper.COLUMN_TYPE +
                " ORDER BY " + DatabaseHelper.COLUMN_DATE;
        Cursor cursor = db.rawQuery(sql, new String[]{ startDate, endDate });
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                String type = cursor.getString(1);
                double amount = cursor.getDouble(2);
                double[] values = map.get(date);
                if (values == null) {
                    values = new double[2];
                    map.put(date, values);
                }
                if (Transaction.TYPE_INCOME.equals(type)) {
                    values[0] = amount;
                } else {
                    values[1] = amount;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return map;
    }

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
        return t;
    }
}
