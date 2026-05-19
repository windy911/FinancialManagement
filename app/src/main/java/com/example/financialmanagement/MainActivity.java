package com.example.financialmanagement;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financialmanagement.adapter.TransactionAdapter;
import com.example.financialmanagement.dao.TransactionDao;
import com.example.financialmanagement.model.Transaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ADD = 1;
    private static final int REQUEST_EDIT = 2;

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private TextView tvSummary;
    private TransactionAdapter adapter;
    private TransactionDao transactionDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        transactionDao = new TransactionDao(this);

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);
        tvSummary = findViewById(R.id.tv_summary);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        findViewById(R.id.btn_statistics).setOnClickListener(v -> openStatistics());
        findViewById(R.id.btn_persons).setOnClickListener(v -> openPersons());

        adapter = new TransactionAdapter();
        adapter.setOnItemClickListener(new TransactionAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Transaction transaction) {
                Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
                intent.putExtra(AddEditActivity.EXTRA_TRANSACTION, transaction);
                startActivityForResult(intent, REQUEST_EDIT);
            }

            @Override
            public void onDeleteClick(Transaction transaction) {
                showDeleteDialog(transaction);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
            startActivityForResult(intent, REQUEST_ADD);
        });

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<Transaction> list = transactionDao.getAll();
        adapter.setTransactions(list);

        if (list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        double totalIncome = transactionDao.getTotalIncome();
        double totalExpense = transactionDao.getTotalExpense();
        double balance = totalIncome - totalExpense;

        String summary = String.format(Locale.getDefault(),
                getString(R.string.total_income) + "\n" +
                getString(R.string.total_expense) + "\n" +
                getString(R.string.balance),
                totalIncome, totalExpense, balance);
        tvSummary.setText(summary);
    }

    private void showDeleteDialog(Transaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        transactionDao.delete(transaction.getId());
                        loadData();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void openStatistics() {
        Intent intent = new Intent(this, StatisticsActivity.class);
        startActivity(intent);
    }

    private void openPersons() {
        Intent intent = new Intent(this, PersonActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadData();
        }
    }
}
