package com.example.financialmanagement;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financialmanagement.adapter.PersonAdapter;
import com.example.financialmanagement.dao.PersonDao;
import com.example.financialmanagement.model.Person;
import com.example.financialmanagement.util.AvatarHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.InputStream;
import java.util.List;

public class PersonActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 100;

    private RecyclerView recyclerView;
    private View tvEmpty;
    private EditText etPersonName;
    private PersonAdapter adapter;
    private PersonDao personDao;
    private Person pendingAvatarPerson;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null && pendingAvatarPerson != null) {
                        saveAvatarFromUri(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        personDao = new PersonDao(this);

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);
        etPersonName = findViewById(R.id.et_person_name);

        adapter = new PersonAdapter();
        adapter.setOnItemClickListener(new PersonAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Person person) {
                showEditDialog(person);
            }

            @Override
            public void onDeleteClick(Person person) {
                showDeleteDialog(person);
            }
        });
        adapter.setOnAvatarClickListener(person -> {
            pendingAvatarPerson = person;
            pickAvatar();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_add_person).setOnClickListener(v -> addPerson());

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<Person> list = personDao.getAll();
        adapter.setPersons(list);

        if (list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void addPerson() {
        String name = etPersonName.getText().toString().trim();
        if (name.isEmpty()) {
            etPersonName.setError("请输入人名");
            return;
        }
        if (personDao.exists(name)) {
            Toast.makeText(this, "该人名已存在", Toast.LENGTH_SHORT).show();
            return;
        }
        personDao.insert(name);
        etPersonName.setText("");
        loadData();
        Toast.makeText(this, "人名已添加", Toast.LENGTH_SHORT).show();
    }

    private void showEditDialog(Person person) {
        final EditText input = new EditText(this);
        input.setText(person.getName());
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_record)
                .setView(input)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = input.getText().toString().trim();
                        if (newName.isEmpty()) {
                            Toast.makeText(PersonActivity.this, "人名不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!newName.equals(person.getName()) && personDao.exists(newName)) {
                            Toast.makeText(PersonActivity.this, "该人名已存在", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        personDao.update(person.getId(), newName);
                        loadData();
                        Toast.makeText(PersonActivity.this, "人名已更新", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDeleteDialog(Person person) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage("确定要删除 " + person.getName() + " 吗？")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        personDao.delete(person.getId());
                        loadData();
                        Toast.makeText(PersonActivity.this, "人名已删除", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void pickAvatar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
                return;
            }
        }
        openImagePicker();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "需要权限才能选择头像", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void saveAvatarFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return;
            Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            if (bitmap == null) return;
            Bitmap scaled = AvatarHelper.scaleBitmap(bitmap, 256);
            String base64 = AvatarHelper.bitmapToBase64(scaled);
            personDao.updateAvatar(pendingAvatarPerson.getId(), base64);
            loadData();
            Toast.makeText(this, "头像已更新", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "头像设置失败", Toast.LENGTH_SHORT).show();
        }
    }
}
