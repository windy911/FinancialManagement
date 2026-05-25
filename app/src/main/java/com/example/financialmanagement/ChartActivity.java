package com.example.financialmanagement;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.financialmanagement.dao.TransactionDao;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChartActivity extends AppCompatActivity {

    private LineChart lineChart;
    private Spinner spinnerRange;
    private TextView tvEmpty;
    private TransactionDao transactionDao;
    private String currentProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        lineChart = findViewById(R.id.line_chart);
        spinnerRange = findViewById(R.id.spinner_range);
        tvEmpty = findViewById(R.id.tv_empty);
        transactionDao = new TransactionDao(this);

        SharedPreferences sp = getSharedPreferences("app_prefs", MODE_PRIVATE);
        currentProject = sp.getString("current_project", "默认项目");

        setupSpinner();
        setupChartStyle();

        spinnerRange.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                loadChart(position);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        loadChart(0);
    }

    private void setupSpinner() {
        String[] ranges = {"最近7天", "最近30天", "最近90天"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ranges);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRange.setAdapter(adapter);
    }

    private void setupChartStyle() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setBackgroundColor(0xFF1E1E1E);

        int textColor = 0xFFFFFFFF;
        int gridColor = 0xFF3C3C3C;

        lineChart.getLegend().setTextSize(12f);
        lineChart.getLegend().setTextColor(textColor);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(textColor);
        xAxis.setAxisLineColor(gridColor);

        YAxis left = lineChart.getAxisLeft();
        left.setDrawGridLines(true);
        left.setGridColor(gridColor);
        left.setAxisMinimum(0f);
        left.setTextColor(textColor);
        left.setAxisLineColor(gridColor);

        lineChart.getAxisRight().setEnabled(false);
    }

    private void loadChart(int rangeIndex) {
        int days = rangeIndex == 0 ? 7 : (rangeIndex == 1 ? 30 : 90);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String endDate = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, -(days - 1));
        String startDate = sdf.format(cal.getTime());

        Map<String, double[]> dataMap = transactionDao.getDailyTrendByProject(startDate, endDate, currentProject);

        if (dataMap.isEmpty()) {
            lineChart.setVisibility(android.view.View.GONE);
            tvEmpty.setVisibility(android.view.View.VISIBLE);
            return;
        }

        lineChart.setVisibility(android.view.View.VISIBLE);
        tvEmpty.setVisibility(android.view.View.GONE);

        fillMissingDates(dataMap, startDate, endDate);

        List<String> labels = new ArrayList<>();
        List<Entry> incomeEntries = new ArrayList<>();
        List<Entry> expenseEntries = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, double[]> entry : dataMap.entrySet()) {
            labels.add(entry.getKey().substring(5));
            incomeEntries.add(new Entry(index, (float) entry.getValue()[0]));
            expenseEntries.add(new Entry(index, (float) entry.getValue()[1]));
            index++;
        }

        LineDataSet incomeSet = new LineDataSet(incomeEntries, getString(R.string.income));
        incomeSet.setColor(getColor(R.color.income_green));
        incomeSet.setCircleColor(getColor(R.color.income_green));
        incomeSet.setLineWidth(2.5f);
        incomeSet.setCircleRadius(3f);
        incomeSet.setDrawValues(false);

        LineDataSet expenseSet = new LineDataSet(expenseEntries, getString(R.string.expense));
        expenseSet.setColor(getColor(R.color.expense_red));
        expenseSet.setCircleColor(getColor(R.color.expense_red));
        expenseSet.setLineWidth(2.5f);
        expenseSet.setCircleRadius(3f);
        expenseSet.setDrawValues(false);

        LineData lineData = new LineData(incomeSet, expenseSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size() > 7 ? 7 : labels.size(), false);

        lineChart.invalidate();
    }

    private void fillMissingDates(Map<String, double[]> map, String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(startDate));
            java.util.Date end = sdf.parse(endDate);
            while (!cal.getTime().after(end)) {
                String date = sdf.format(cal.getTime());
                if (!map.containsKey(date)) {
                    map.put(date, new double[]{0, 0});
                }
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
