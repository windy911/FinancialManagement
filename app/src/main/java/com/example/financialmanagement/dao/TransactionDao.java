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
        values.put(DatabaseHelper.COLUMN_PROJECT, transaction.getProject());
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
        values.put(DatabaseHelper.COLUMN_PROJECT, transaction.getProject());
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

    public List<Transaction> getByDateAndProject(String date, String project) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_DATE + " = ? AND " + DatabaseHelper.COLUMN_PROJECT + " = ?";
        String[] selectionArgs = { date, project };
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

    public List<Transaction> getByYearMonthAndProject(String yearMonth, String project) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_DATE + " LIKE ? AND " + DatabaseHelper.COLUMN_PROJECT + " = ?";
        String[] selectionArgs = { yearMonth + "%", project };
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

    public List<Transaction> getByDateRangeAndProject(String startDate, String endDate, String project) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_DATE + " >= ? AND " + DatabaseHelper.COLUMN_DATE + " <= ? AND "
                + DatabaseHelper.COLUMN_PROJECT + " = ?";
        String[] selectionArgs = { startDate, endDate, project };
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

    public List<Transaction> getByYearAndProject(String year, String project) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_DATE + " LIKE ? AND " + DatabaseHelper.COLUMN_PROJECT + " = ?";
        String[] selectionArgs = { year + "%", project };
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

    public double getTotalIncomeByProject(String project) {
        return getTotalByTypeAndProject(Transaction.TYPE_INCOME, project);
    }

    public double getTotalExpenseByProject(String project) {
        return getTotalByTypeAndProject(Transaction.TYPE_EXPENSE, project);
    }

    public double getTotalIncomeByDateAndProject(String date, String project) {
        return getTotalByTypeAndDateAndProject(Transaction.TYPE_INCOME, date, project);
    }

    public double getTotalExpenseByDateAndProject(String date, String project) {
        return getTotalByTypeAndDateAndProject(Transaction.TYPE_EXPENSE, date, project);
    }

    public double getTotalIncomeByYearMonthAndProject(String yearMonth, String project) {
        return getTotalByTypeAndYearMonthAndProject(Transaction.TYPE_INCOME, yearMonth, project);
    }

    public double getTotalExpenseByYearMonthAndProject(String yearMonth, String project) {
        return getTotalByTypeAndYearMonthAndProject(Transaction.TYPE_EXPENSE, yearMonth, project);
    }

    public double getTotalIncomeByYearAndProject(String year, String project) {
        return getTotalByTypeAndYearAndProject(Transaction.TYPE_INCOME, year, project);
    }

    public double getTotalExpenseByYearAndProject(String year, String project) {
        return getTotalByTypeAndYearAndProject(Transaction.TYPE_EXPENSE, year, project);
    }

    public double getTotalIncomeByDateRangeAndProject(String startDate, String endDate, String project) {
        return getTotalByTypeAndDateRangeAndProject(Transaction.TYPE_INCOME, startDate, endDate, project);
    }

    public double getTotalExpenseByDateRangeAndProject(String startDate, String endDate, String project) {
        return getTotalByTypeAndDateRangeAndProject(Transaction.TYPE_EXPENSE, startDate, endDate, project);
    }

    public List<Transaction> getByPersonAndProject(String person, String project) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_PERSON + " = ? AND " + DatabaseHelper.COLUMN_PROJECT + " = ?";
        String[] selectionArgs = { person, project };
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

    public double getTotalIncomeByPersonAndProject(String person, String project) {
        return getTotalByTypeAndPersonAndProject(Transaction.TYPE_INCOME, person, project);
    }

    public double getTotalExpenseByPersonAndProject(String person, String project) {
        return getTotalByTypeAndPersonAndProject(Transaction.TYPE_EXPENSE, person, project);
    }

    private double getTotalByTypeAndPersonAndProject(String type, String person, String project) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TYPE + " = ? AND "
                + DatabaseHelper.COLUMN_PERSON + " = ? AND " + DatabaseHelper.COLUMN_PROJECT + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{ type, person, project });
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    private double getTotalByTypeAndProject(String type, String project) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TYPE + " = ? AND " + DatabaseHelper.COLUMN_PROJECT + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{ type, project });
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    private double getTotalByTypeAndDateAndProject(String type, String date, String project) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TYPE + " = ? AND "
                + DatabaseHelper.COLUMN_DATE + " = ? AND " + DatabaseHelper.COLUMN_PROJECT + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{ type, date, project });
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    private double getTotalByTypeAndYearMonthAndProject(String type, String yearMonth, String project) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TYPE + " = ? AND "
                + DatabaseHelper.COLUMN_DATE + " LIKE ? AND " + DatabaseHelper.COLUMN_PROJECT + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{ type, yearMonth + "%", project });
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    private double getTotalByTypeAndDateRangeAndProject(String type, String startDate, String endDate, String project) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TYPE + " = ? AND "
                + DatabaseHelper.COLUMN_DATE + " >= ? AND " + DatabaseHelper.COLUMN_DATE + " <= ? AND "
                + DatabaseHelper.COLUMN_PROJECT + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{ type, startDate, endDate, project });
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    private double getTotalByTypeAndYearAndProject(String type, String year, String project) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TYPE + " = ? AND "
                + DatabaseHelper.COLUMN_DATE + " LIKE ? AND " + DatabaseHelper.COLUMN_PROJECT + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{ type, year + "%", project });
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    public java.util.Map<String, double[]> getDailyTrendByProject(String startDate, String endDate, String project) {
        java.util.Map<String, double[]> map = new java.util.TreeMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT " + DatabaseHelper.COLUMN_DATE + ", " + DatabaseHelper.COLUMN_TYPE +
                ", SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") " +
                "FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_DATE + " >= ? AND " + DatabaseHelper.COLUMN_DATE + " <= ? AND "
                + DatabaseHelper.COLUMN_PROJECT + " = ?" +
                " GROUP BY " + DatabaseHelper.COLUMN_DATE + ", " + DatabaseHelper.COLUMN_TYPE +
                " ORDER BY " + DatabaseHelper.COLUMN_DATE;
        Cursor cursor = db.rawQuery(sql, new String[]{ startDate, endDate, project });
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

    public List<com.example.financialmanagement.model.PersonIncome> getIncomeRankingByDate(String date, String project) {
        return getIncomeRanking(
                DatabaseHelper.COLUMN_DATE + " = ? AND " + DatabaseHelper.COLUMN_PROJECT + " = ?",
                new String[]{ date, project }
        );
    }

    public List<com.example.financialmanagement.model.PersonIncome> getIncomeRankingByDateRange(String startDate, String endDate, String project) {
        return getIncomeRanking(
                DatabaseHelper.COLUMN_DATE + " >= ? AND " + DatabaseHelper.COLUMN_DATE + " <= ? AND "
                        + DatabaseHelper.COLUMN_PROJECT + " = ?",
                new String[]{ startDate, endDate, project }
        );
    }

    public List<com.example.financialmanagement.model.PersonIncome> getIncomeRankingByYearMonth(String yearMonth, String project) {
        return getIncomeRanking(
                DatabaseHelper.COLUMN_DATE + " LIKE ? AND " + DatabaseHelper.COLUMN_PROJECT + " = ?",
                new String[]{ yearMonth + "%", project }
        );
    }

    public List<com.example.financialmanagement.model.PersonIncome> getIncomeRankingByYear(String year, String project) {
        return getIncomeRanking(
                DatabaseHelper.COLUMN_DATE + " LIKE ? AND " + DatabaseHelper.COLUMN_PROJECT + " = ?",
                new String[]{ year + "%", project }
        );
    }

    private List<com.example.financialmanagement.model.PersonIncome> getIncomeRanking(String whereClause, String[] whereArgs) {
        List<com.example.financialmanagement.model.PersonIncome> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT t." + DatabaseHelper.COLUMN_PERSON
                + ", SUM(t." + DatabaseHelper.COLUMN_AMOUNT + ") as total"
                + ", COUNT(*) as cnt"
                + ", p." + DatabaseHelper.COLUMN_PERSON_AVATAR
                + " FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " t"
                + " LEFT JOIN " + DatabaseHelper.TABLE_PERSONS + " p"
                + " ON t." + DatabaseHelper.COLUMN_PERSON + " = p." + DatabaseHelper.COLUMN_PERSON_NAME
                + " WHERE t." + DatabaseHelper.COLUMN_TYPE + " = ? AND " + whereClause
                + " GROUP BY t." + DatabaseHelper.COLUMN_PERSON
                + " ORDER BY total DESC";
        String[] args = new String[whereArgs.length + 1];
        args[0] = Transaction.TYPE_INCOME;
        System.arraycopy(whereArgs, 0, args, 1, whereArgs.length);
        Cursor cursor = db.rawQuery(sql, args);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                double total = cursor.getDouble(1);
                int count = cursor.getInt(2);
                String avatar = cursor.getString(3);
                list.add(new com.example.financialmanagement.model.PersonIncome(name, total, count, avatar));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
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
        t.setProject(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT)));
        return t;
    }
}
