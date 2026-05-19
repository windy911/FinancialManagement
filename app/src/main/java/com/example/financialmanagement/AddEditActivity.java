package com.example.financialmanagement;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.financialmanagement.dao.PersonDao;
import com.example.financialmanagement.dao.TransactionDao;
import com.example.financialmanagement.model.Person;
import com.example.financialmanagement.model.Transaction;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddEditActivity extends AppCompatActivity {

    public static final String EXTRA_TRANSACTION = "transaction";

    private RadioGroup rgType;
    private RadioButton rbIncome, rbExpense;
    private EditText etAmount, etPerson, etEvent, etDate, etTime;
    private Button btnSave, btnCancel, btnSelectPerson;

    private TransactionDao transactionDao;
    private PersonDao personDao;
    private Transaction editingTransaction;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        transactionDao = new TransactionDao(this);
        personDao = new PersonDao(this);
        calendar = Calendar.getInstance();

        rgType = findViewById(R.id.rg_type);
        rbIncome = findViewById(R.id.rb_income);
        rbExpense = findViewById(R.id.rb_expense);
        etAmount = findViewById(R.id.et_amount);
        etPerson = findViewById(R.id.et_person);
        etEvent = findViewById(R.id.et_event);
        etDate = findViewById(R.id.et_date);
        etTime = findViewById(R.id.et_time);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSelectPerson = findViewById(R.id.btn_select_person);

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveTransaction());
        btnCancel.setOnClickListener(v -> finish());
        btnSelectPerson.setOnClickListener(v -> showPersonPicker());

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_TRANSACTION)) {
            editingTransaction = (Transaction) intent.getSerializableExtra(EXTRA_TRANSACTION);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.edit_record);
            }
            fillData(editingTransaction);
        } else {
            updateDateTimeFields();
        }
    }

    private void fillData(Transaction transaction) {
        if (transaction.isIncome()) {
            rbIncome.setChecked(true);
        } else {
            rbExpense.setChecked(true);
        }
        etAmount.setText(String.valueOf(transaction.getAmount()));
        etPerson.setText(transaction.getPerson());
        etEvent.setText(transaction.getEvent());
        etDate.setText(transaction.getDate());
        etTime.setText(transaction.getTime());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            calendar.setTime(sdf.parse(transaction.getDate() + " " + transaction.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDateTimeFields() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        etDate.setText(dateFormat.format(calendar.getTime()));
        etTime.setText(timeFormat.format(calendar.getTime()));
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    updateDateTimeFields();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    updateDateTimeFields();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        dialog.show();
    }

    private void showPersonPicker() {
        List<Person> persons = personDao.getAll();
        if (persons.isEmpty()) {
            Toast.makeText(this, "暂无人名，请先在人名管理中添加", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> names = new ArrayList<>();
        for (Person p : persons) {
            names.add(p.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        new AlertDialog.Builder(this)
                .setTitle("选择人名")
                .setAdapter(adapter, (dialog, which) -> {
                    etPerson.setText(names.get(which));
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        String person = etPerson.getText().toString().trim();
        String event = etEvent.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();

        if (amountStr.isEmpty()) {
            etAmount.setError("请输入金额");
            return;
        }
        if (person.isEmpty()) {
            etPerson.setError("请输入人名");
            return;
        }
        if (event.isEmpty()) {
            etEvent.setError("请输入事件");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError("金额格式错误");
            return;
        }

        String type = rbIncome.isChecked() ? Transaction.TYPE_INCOME : Transaction.TYPE_EXPENSE;

        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setPerson(person);
        transaction.setEvent(event);
        transaction.setDate(date);
        transaction.setTime(time);
        transaction.setTimestamp(calendar.getTimeInMillis());

        if (editingTransaction != null) {
            transaction.setId(editingTransaction.getId());
            transactionDao.update(transaction);
            Toast.makeText(this, "记录已更新", Toast.LENGTH_SHORT).show();
        } else {
            transactionDao.insert(transaction);
            Toast.makeText(this, "记录已添加", Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish();
    }
}
