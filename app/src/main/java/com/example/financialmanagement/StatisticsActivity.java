package com.example.financialmanagement;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financialmanagement.adapter.RankingAdapter;
import com.example.financialmanagement.adapter.TransactionAdapter;
import com.example.financialmanagement.dao.PersonDao;
import com.example.financialmanagement.dao.TransactionDao;
import com.example.financialmanagement.model.Person;
import com.example.financialmanagement.model.PersonIncome;
import com.example.financialmanagement.model.Transaction;
import com.example.financialmanagement.util.ReportHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private Button btnGenerateReport;
    private TransactionAdapter adapter;
    private TransactionDao transactionDao;
    private PersonDao personDao;

    private List<Transaction> allTransactions;
    private List<Transaction> currentFilteredList = new ArrayList<>();
    private List<String> periodValues = new ArrayList<>();
    private List<String> personNames = new ArrayList<>();
    private static final String ALL_PERSONS = "全部";
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_CURRENT_PROJECT = "current_project";

    private static final String PERIOD_YEAR = "按年";
    private static final String PERIOD_MONTH = "按月";
    private static final String PERIOD_WEEK = "按周";
    private static final String PERIOD_DAY = "按日";
    private static final String VIEW_DETAIL = "明细";
    private static final String VIEW_RANKING = "排行";

    private String currentProject;
    private Spinner spinnerViewMode;
    private RankingAdapter rankingAdapter;

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

        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentProject = sp.getString(KEY_CURRENT_PROJECT, "默认项目");

        allTransactions = transactionDao.getAllByProject(currentProject);

        spinnerViewMode = findViewById(R.id.spinner_view_mode);
        spinnerPeriod = findViewById(R.id.spinner_period);
        spinnerValue = findViewById(R.id.spinner_value);
        spinnerPerson = findViewById(R.id.spinner_person);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
        tvBalance = findViewById(R.id.tv_balance);
        tvEmpty = findViewById(R.id.tv_empty);
        recyclerView = findViewById(R.id.recycler_view);
        btnGenerateReport = findViewById(R.id.btn_generate_report);

        adapter = new TransactionAdapter();
        rankingAdapter = new RankingAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // View mode spinner
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

        btnGenerateReport.setOnClickListener(v -> generateAndShowReport());

        List<String> periods = new ArrayList<>();
        periods.add(PERIOD_YEAR);
        periods.add(PERIOD_MONTH);
        periods.add(PERIOD_WEEK);
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
                String mode = (String) spinnerViewMode.getSelectedItem();
                if (VIEW_RANKING.equals(mode)) {
                    loadRanking();
                } else {
                    updateStatistics();
                }
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
            } else if (PERIOD_WEEK.equals(period)) {
                valuesSet.add(getMondayOfDate(date));
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
            updateViewMode();
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
                income = transactionDao.getTotalIncomeByYearAndProject(value, currentProject);
                expense = transactionDao.getTotalExpenseByYearAndProject(value, currentProject);
                list = transactionDao.getByYearAndProject(value, currentProject);
            } else {
                list = filterByPerson(transactionDao.getByYearAndProject(value, currentProject), selectedPerson);
                income = sumByType(list, Transaction.TYPE_INCOME);
                expense = sumByType(list, Transaction.TYPE_EXPENSE);
            }
        } else if (PERIOD_MONTH.equals(period)) {
            if (ALL_PERSONS.equals(selectedPerson)) {
                income = transactionDao.getTotalIncomeByYearMonthAndProject(value, currentProject);
                expense = transactionDao.getTotalExpenseByYearMonthAndProject(value, currentProject);
                list = transactionDao.getByYearMonthAndProject(value, currentProject);
            } else {
                list = filterByPerson(transactionDao.getByYearMonthAndProject(value, currentProject), selectedPerson);
                income = sumByType(list, Transaction.TYPE_INCOME);
                expense = sumByType(list, Transaction.TYPE_EXPENSE);
            }
        } else if (PERIOD_WEEK.equals(period)) {
            String startDate = value;
            String endDate = getSundayOfDate(value);
            if (ALL_PERSONS.equals(selectedPerson)) {
                income = transactionDao.getTotalIncomeByDateRangeAndProject(startDate, endDate, currentProject);
                expense = transactionDao.getTotalExpenseByDateRangeAndProject(startDate, endDate, currentProject);
                list = transactionDao.getByDateRangeAndProject(startDate, endDate, currentProject);
            } else {
                list = filterByPerson(transactionDao.getByDateRangeAndProject(startDate, endDate, currentProject), selectedPerson);
                income = sumByType(list, Transaction.TYPE_INCOME);
                expense = sumByType(list, Transaction.TYPE_EXPENSE);
            }
        } else {
            if (ALL_PERSONS.equals(selectedPerson)) {
                income = transactionDao.getTotalIncomeByDateAndProject(value, currentProject);
                expense = transactionDao.getTotalExpenseByDateAndProject(value, currentProject);
                list = transactionDao.getByDateAndProject(value, currentProject);
            } else {
                list = filterByPerson(transactionDao.getByDateAndProject(value, currentProject), selectedPerson);
                income = sumByType(list, Transaction.TYPE_INCOME);
                expense = sumByType(list, Transaction.TYPE_EXPENSE);
            }
        }

        double balance = income - expense;

        tvTotalIncome.setText(String.format(Locale.getDefault(), getString(R.string.total_income), income));
        tvTotalExpense.setText(String.format(Locale.getDefault(), getString(R.string.total_expense), expense));
        tvBalance.setText(String.format(Locale.getDefault(), getString(R.string.balance), balance));

        adapter.setTransactions(list);
        currentFilteredList = list;

        if (list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void updateViewMode() {
        String mode = (String) spinnerViewMode.getSelectedItem();
        if (VIEW_RANKING.equals(mode)) {
            spinnerPerson.setVisibility(View.GONE);
            btnGenerateReport.setVisibility(View.GONE);
            loadRanking();
        } else {
            spinnerPerson.setVisibility(View.VISIBLE);
            btnGenerateReport.setVisibility(View.VISIBLE);
            updateStatistics();
        }
    }

    private void loadRanking() {
        if (periodValues.isEmpty()) {
            showEmptyState();
            return;
        }

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

    private void generateAndShowReport() {
        if (currentFilteredList == null || currentFilteredList.isEmpty()) {
            Toast.makeText(this, R.string.report_no_data, Toast.LENGTH_SHORT).show();
            return;
        }

        String period = (String) spinnerPeriod.getSelectedItem();
        String value = (String) spinnerValue.getSelectedItem();
        String selectedPerson = (String) spinnerPerson.getSelectedItem();

        ReportHelper.PeriodType periodType;
        if (PERIOD_DAY.equals(period)) {
            periodType = ReportHelper.PeriodType.TODAY;
        } else if (PERIOD_WEEK.equals(period)) {
            periodType = ReportHelper.PeriodType.WEEK;
        } else if (PERIOD_MONTH.equals(period)) {
            periodType = ReportHelper.PeriodType.MONTH;
        } else {
            periodType = ReportHelper.PeriodType.MONTH;
        }

        String report = ReportHelper.generateReport(currentFilteredList, value, periodType, selectedPerson);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.report_title)
                .setMessage(report)
                .setPositiveButton(R.string.copy_report, (d, which) -> {
                    copyToClipboard(report);
                })
                .setNegativeButton(R.string.close, null)
                .create();

        dialog.show();

        // 设置对话框中消息文本的字体大小和颜色以适配暗黑主题
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setTextSize(13f);
            messageView.setTextColor(getColor(R.color.on_surface));
        }
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(getString(R.string.report_title), text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, R.string.report_copied, Toast.LENGTH_SHORT).show();
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

    private String getMondayOfDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(dateStr));
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int daysToSubtract = dayOfWeek - Calendar.MONDAY;
            if (daysToSubtract < 0) daysToSubtract += 7;
            cal.add(Calendar.DAY_OF_MONTH, -daysToSubtract);
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return dateStr;
        }
    }

    private String getSundayOfDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(dateStr));
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int daysToAdd = Calendar.SUNDAY - dayOfWeek;
            if (daysToAdd < 0) daysToAdd += 7;
            cal.add(Calendar.DAY_OF_MONTH, daysToAdd);
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return dateStr;
        }
    }

    private void showEmptyState() {
        tvTotalIncome.setText(String.format(Locale.getDefault(), getString(R.string.total_income), 0.0));
        tvTotalExpense.setText(String.format(Locale.getDefault(), getString(R.string.total_expense), 0.0));
        tvBalance.setText(String.format(Locale.getDefault(), getString(R.string.balance), 0.0));
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        currentFilteredList = new ArrayList<>();
    }
}
