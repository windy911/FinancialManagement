package com.example.financialmanagement;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financialmanagement.adapter.TransactionAdapter;
import com.example.financialmanagement.dao.ProjectDao;
import com.example.financialmanagement.dao.TransactionDao;
import com.example.financialmanagement.model.Project;
import com.example.financialmanagement.model.Transaction;
import com.example.financialmanagement.util.BackupHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ADD = 1;
    private static final int REQUEST_EDIT = 2;
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_CURRENT_PROJECT = "current_project";

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private TextView tvSummary;
    private TransactionAdapter adapter;
    private TransactionDao transactionDao;
    private ProjectDao projectDao;
    private Spinner spinnerProject;
    private String currentProject;

    private final ActivityResultLauncher<Intent> backupLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        BackupHelper.performBackup(this, uri, new BackupHelper.BackupCallback() {
                            @Override
                            public void onSuccess(String message) {
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                            @Override
                            public void onError(String error) {
                                Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });

    private final ActivityResultLauncher<Intent> restoreLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        showRestoreConfirmDialog(uri);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> exportJsonLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        BackupHelper.exportToJson(this, uri, new BackupHelper.BackupCallback() {
                            @Override
                            public void onSuccess(String message) {
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                            @Override
                            public void onError(String error) {
                                Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        transactionDao = new TransactionDao(this);
        projectDao = new ProjectDao(this);

        // 读取上次使用的项目
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentProject = sp.getString(KEY_CURRENT_PROJECT, "默认项目");

        spinnerProject = findViewById(R.id.spinner_project);
        setupProjectSpinner();

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);
        tvSummary = findViewById(R.id.tv_summary);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        findViewById(R.id.btn_statistics).setOnClickListener(v -> openStatistics());
        findViewById(R.id.btn_chart).setOnClickListener(v -> openChart());
        findViewById(R.id.btn_persons).setOnClickListener(v -> openPersons());
        findViewById(R.id.btn_events).setOnClickListener(v -> openEvents());

        adapter = new TransactionAdapter();
        adapter.setPersonDao(new com.example.financialmanagement.dao.PersonDao(this));
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
        setupProjectSpinner();
        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_backup) {
            startBackup();
            return true;
        } else if (id == R.id.action_restore) {
            startRestore();
            return true;
        } else if (id == R.id.action_export_json) {
            startExportJson();
            return true;
        } else if (id == R.id.action_manage_projects) {
            openProjects();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startBackup() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_TITLE, BackupHelper.generateBackupFileName());
        backupLauncher.launch(intent);
    }

    private void startRestore() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        restoreLauncher.launch(intent);
    }

    private void startExportJson() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, BackupHelper.generateBackupFileName().replace(".db", ".json"));
        exportJsonLauncher.launch(intent);
    }

    private void showRestoreConfirmDialog(Uri uri) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.restore_data)
                .setMessage(R.string.confirm_restore)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    BackupHelper.performRestore(this, uri, new BackupHelper.BackupCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                            loadData();
                        }
                        @Override
                        public void onError(String error) {
                            Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void loadData() {
        List<Transaction> list = transactionDao.getAllByProject(currentProject);
        adapter.setTransactions(list);

        if (list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        double totalIncome = transactionDao.getTotalIncomeByProject(currentProject);
        double totalExpense = transactionDao.getTotalExpenseByProject(currentProject);
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

    private void openEvents() {
        Intent intent = new Intent(this, EventActivity.class);
        startActivity(intent);
    }

    private void openChart() {
        Intent intent = new Intent(this, ChartActivity.class);
        startActivity(intent);
    }

    private void openProjects() {
        Intent intent = new Intent(this, ProjectActivity.class);
        startActivity(intent);
    }

    private void setupProjectSpinner() {
        List<Project> projects = projectDao.getAll();
        List<String> projectNames = new java.util.ArrayList<>();
        for (Project p : projects) {
            projectNames.add(p.getName());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, projectNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProject.setAdapter(spinnerAdapter);

        int position = projectNames.indexOf(currentProject);
        if (position >= 0) {
            spinnerProject.setSelection(position);
        }

        spinnerProject.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                String selected = projectNames.get(pos);
                if (!selected.equals(currentProject)) {
                    currentProject = selected;
                    SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    sp.edit().putString(KEY_CURRENT_PROJECT, currentProject).apply();
                    loadData();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        showExitConfirmDialog();
    }

    private void showExitConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_exit_title)
                .setMessage(R.string.confirm_exit_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> super.onBackPressed())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadData();
        }
    }
}
