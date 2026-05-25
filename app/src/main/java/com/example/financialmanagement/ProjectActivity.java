package com.example.financialmanagement;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financialmanagement.adapter.ProjectAdapter;
import com.example.financialmanagement.dao.ProjectDao;
import com.example.financialmanagement.model.Project;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class ProjectActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_CURRENT_PROJECT = "current_project";

    private RecyclerView recyclerView;
    private View tvEmpty;
    private EditText etProjectName;
    private ProjectAdapter adapter;
    private ProjectDao projectDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        projectDao = new ProjectDao(this);

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);
        etProjectName = findViewById(R.id.et_project_name);

        adapter = new ProjectAdapter();
        adapter.setOnItemClickListener(new ProjectAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Project project) {
                showEditDialog(project);
            }

            @Override
            public void onDeleteClick(Project project) {
                showDeleteDialog(project);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_add_project).setOnClickListener(v -> addProject());

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<Project> list = projectDao.getAll();
        adapter.setProjects(list);

        if (list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void addProject() {
        String name = etProjectName.getText().toString().trim();
        if (name.isEmpty()) {
            etProjectName.setError("请输入项目名称");
            return;
        }
        if (projectDao.exists(name)) {
            Toast.makeText(this, R.string.project_exists, Toast.LENGTH_SHORT).show();
            return;
        }
        projectDao.insert(name);
        etProjectName.setText("");
        loadData();
        Toast.makeText(this, R.string.project_added, Toast.LENGTH_SHORT).show();
    }

    private void showEditDialog(Project project) {
        final EditText input = new EditText(this);
        input.setText(project.getName());
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_record)
                .setView(input)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = input.getText().toString().trim();
                        if (newName.isEmpty()) {
                            Toast.makeText(ProjectActivity.this, "项目名称不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!newName.equals(project.getName()) && projectDao.exists(newName)) {
                            Toast.makeText(ProjectActivity.this, R.string.project_exists, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        projectDao.update(project.getId(), newName);
                        loadData();
                        Toast.makeText(ProjectActivity.this, R.string.project_updated, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDeleteDialog(Project project) {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentProject = sp.getString(KEY_CURRENT_PROJECT, "默认项目");
        if (project.getName().equals(currentProject)) {
            Toast.makeText(this, R.string.cannot_delete_current_project, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage("确定要删除 " + project.getName() + " 吗？")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        projectDao.delete(project.getId());
                        loadData();
                        Toast.makeText(ProjectActivity.this, R.string.project_deleted, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
