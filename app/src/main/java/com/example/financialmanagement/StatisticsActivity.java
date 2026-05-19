package com.example.financialmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financialmanagement.adapter.TransactionAdapter;
import com.example.financialmanagement.dao.PersonDao;
import com.example.financialmanagement.dao.TransactionDao;
import com.example.financialmanagement.model.Person;
import com.example.financialmanagement.model.Transaction;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StatisticsActivity extends AppCompatActivity {

    private Spinner spinnerPeriod;
    private Spinner spinnerValue;
    private Spinner spinnerPerson;
    private TextView tvTotalIncome, tvTotalExpense, tvBalance, tvEmpty;
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private TransactionDao transactionDao;
    private PersonDao personDao;

    private List<Transaction> allTransactions;
    private List<String> periodValues = new ArrayList<>();
    private List<String> personNames = new ArrayList<>();
    private static final String ALL_PERSONS = "全部";

    private static final String PERIOD_YEAR = "按年";
    private static final String PERIOD_MONTH = "按月";
    private static final String PERIOD_DAY = "按日";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        transactionDao = new TransactionDao(this);
        personDao = new PersonDao(this);
        allTransactions = transactionDao.getAll();

        spinnerPeriod = findViewById(R.id.spinner_period);
        spinnerValue = findViewById(R.id.spinner_value);
        spinnerPerson = findViewById(R.id.spinner_person);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
        tvBalance = findViewById(R.id.tv_balance);
        tvEmpty = findViewById(R.id.tv_empty);
        recyclerView = findViewById(R.id.recycler_view);

        adapter = new TransactionAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        List<String> periods = new ArrayList<>();
        periods.add(PERIOD_YEAR);
        periods.add(PERIOD_MONTH);
        periods.add(PERIOD_DAY);
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, periods);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(periodAdapter);

        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadPeriodValues(periods.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateStatistics();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        loadPersonSpinner();
        spinnerPerson.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateStatistics();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (!allTransactions.isEmpty()) {
            spinnerPeriod.setSelection(2);
            loadPeriodValues(PERIOD_DAY);
        } else {
            showEmptyState();
        }
    }

    private void loadPersonSpinner() {
        personNames.clear();
        personNames.add(ALL_PERSONS);
        List<Person> persons = personDao.getAll();
        for (Person p : persons) {
            personNames.add(p.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, personNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPerson.setAdapter(adapter);
    }

    private void loadPeriodValues(String period) {
        Set<String> valuesSet = new LinkedHashSet<>();
        for (Transaction t : allTransactions) {
            String date = t.getDate();
            if (date == null || date.length() < 10) continue;
            if (PERIOD_YEAR.equals(period)) {
                valuesSet.add(date.substring(0, 4));
            } else if (PERIOD_MONTH.equals(period)) {
                valuesSet.add(date.substring(0, 7));
            } else if (PERIOD_DAY.equals(period)) {
                valuesSet.add(date);
            }
        }
        periodValues = new ArrayList<>(valuesSet);
        Collections.sort(periodValues, Collections.reverseOrder());

        ArrayAdapter<String> valueAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, periodValues);
        valueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerValue.setAdapter(valueAdapter);

        if (!periodValues.isEmpty()) {
            spinnerValue.setSelection(0);
            updateStatistics();
        } else {
            showEmptyState();
        }
    }

    private void updateStatistics() {
        if (periodValues.isEmpty()) {
            showEmptyState();
            return;
        }

        String period = (String) spinnerPeriod.getSelectedItem();
        String value = (String) spinnerValue.getSelectedItem();
        String selectedPerson = (String) spinnerPerson.getSelectedItem();

        double income, expense;
        List<Transaction> list;

        if (PERIOD_YEAR.equals(period)) {
            if (ALL_PERSONS.equals(selectedPerson)) {
                income = transactionDao.getTotalIncomeByYear(value);
                expense = transactionDao.getTotalExpenseByYear(value);
                list = transactionDao.getByYear(value);
            } else {
                list = filterByPerson(transactionDao.getByYear(value), selectedPerson);
                income = sumByType(list, Transaction.TYPE_INCOME);
                expense = sumByType(list, Transaction.TYPE_EXPENSE);
            }
        } else if (PERIOD_MONTH.equals(period)) {
            if (ALL_PERSONS.equals(selectedPerson)) {
                income = transactionDao.getTotalIncomeByYearMonth(value);
                expense = transactionDao.getTotalExpenseByYearMonth(value);
                list = transactionDao.getByYearMonth(value);
            } else {
                list = filterByPerson(transactionDao.getByYearMonth(value), selectedPerson);
                income = sumByType(list, Transaction.TYPE_INCOME);
                expense = sumByType(list, Transaction.TYPE_EXPENSE);
            }
        } else {
            if (ALL_PERSONS.equals(selectedPerson)) {
                income = transactionDao.getTotalIncomeByDate(value);
                expense = transactionDao.getTotalExpenseByDate(value);
                list = transactionDao.getByDate(value);
            } else {
                list = filterByPerson(transactionDao.getByDate(value), selectedPerson);
                income = sumByType(list, Transaction.TYPE_INCOME);
                expense = sumByType(list, Transaction.TYPE_EXPENSE);
            }
        }

        double balance = income - expense;

        tvTotalIncome.setText(String.format(Locale.getDefault(), getString(R.string.total_income), income));
        tvTotalExpense.setText(String.format(Locale.getDefault(), getString(R.string.total_expense), expense));
        tvBalance.setText(String.format(Locale.getDefault(), getString(R.string.balance), balance));

        adapter.setTransactions(list);

        if (list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private List<Transaction> filterByPerson(List<Transaction> list, String person) {
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : list) {
            if (person.equals(t.getPerson())) {
                filtered.add(t);
            }
        }
        return filtered;
    }

    private double sumByType(List<Transaction> list, String type) {
        double total = 0;
        for (Transaction t : list) {
            if (type.equals(t.getType())) {
                total += t.getAmount();
            }
        }
        return total;
    }

    private void showEmptyState() {
        tvTotalIncome.setText(String.format(Locale.getDefault(), getString(R.string.total_income), 0.0));
        tvTotalExpense.setText(String.format(Locale.getDefault(), getString(R.string.total_expense), 0.0));
        tvBalance.setText(String.format(Locale.getDefault(), getString(R.string.balance), 0.0));
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
    }
}
