package com.example.financialmanagement;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financialmanagement.adapter.EventAdapter;
import com.example.financialmanagement.dao.EventDao;
import com.example.financialmanagement.model.Event;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class EventActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View tvEmpty;
    private EditText etEventName;
    private EventAdapter adapter;
    private EventDao eventDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        eventDao = new EventDao(this);

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);
        etEventName = findViewById(R.id.et_event_name);

        adapter = new EventAdapter();
        adapter.setOnItemClickListener(new EventAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Event event) {
                showEditDialog(event);
            }

            @Override
            public void onDeleteClick(Event event) {
                showDeleteDialog(event);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_add_event).setOnClickListener(v -> addEvent());

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<Event> list = eventDao.getAll();
        adapter.setEvents(list);

        if (list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void addEvent() {
        String name = etEventName.getText().toString().trim();
        if (name.isEmpty()) {
            etEventName.setError("请输入事件名称");
            return;
        }
        if (eventDao.exists(name)) {
            Toast.makeText(this, "该事件已存在", Toast.LENGTH_SHORT).show();
            return;
        }
        eventDao.insert(name);
        etEventName.setText("");
        loadData();
        Toast.makeText(this, "事件已添加", Toast.LENGTH_SHORT).show();
    }

    private void showEditDialog(Event event) {
        final EditText input = new EditText(this);
        input.setText(event.getName());
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_record)
                .setView(input)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(EventActivity.this, "事件名称不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newName.equals(event.getName()) && eventDao.exists(newName)) {
                        Toast.makeText(EventActivity.this, "该事件已存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    eventDao.update(event.getId(), newName);
                    loadData();
                    Toast.makeText(EventActivity.this, "事件已更新", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDeleteDialog(Event event) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage("确定要删除 " + event.getName() + " 吗？")
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    eventDao.delete(event.getId());
                    loadData();
                    Toast.makeText(EventActivity.this, "事件已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
