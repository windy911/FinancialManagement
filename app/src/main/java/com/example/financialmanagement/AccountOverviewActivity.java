package com.example.financialmanagement;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class AccountOverviewActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "account_overview";

    private TextInputEditText etWechat, etAlipay, etCash, etMeituan, etPromotion, etTotal, etOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_overview);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etWechat = findViewById(R.id.et_wechat);
        etAlipay = findViewById(R.id.et_alipay);
        etCash = findViewById(R.id.et_cash);
        etMeituan = findViewById(R.id.et_meituan);
        etPromotion = findViewById(R.id.et_promotion);
        etTotal = findViewById(R.id.et_total);
        etOrders = findViewById(R.id.et_orders);

        loadData();

        Button btnGenerate = findViewById(R.id.btn_generate);
        btnGenerate.setOnClickListener(v -> generateAndCopy());
    }

    private void loadData() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        etWechat.setText(sp.getString("wechat", ""));
        etAlipay.setText(sp.getString("alipay", ""));
        etCash.setText(sp.getString("cash", ""));
        etMeituan.setText(sp.getString("meituan", ""));
        etPromotion.setText(sp.getString("promotion", ""));
        etTotal.setText(sp.getString("total", ""));
        etOrders.setText(sp.getString("orders", ""));
    }

    private void saveData() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        sp.edit()
                .putString("wechat", getText(etWechat))
                .putString("alipay", getText(etAlipay))
                .putString("cash", getText(etCash))
                .putString("meituan", getText(etMeituan))
                .putString("promotion", getText(etPromotion))
                .putString("total", getText(etTotal))
                .putString("orders", getText(etOrders))
                .apply();
    }

    private void generateAndCopy() {
        saveData();

        String text = "微信 " + getText(etWechat)
                + " | 支付宝 " + getText(etAlipay)
                + " | 现金 " + getText(etCash)
                + " | 美团 " + getText(etMeituan)
                + " | 推广通 " + getText(etPromotion)
                + " | 总账 " + getText(etTotal)
                + " | 当月单量 " + getText(etOrders);

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("account_overview", text));
            Snackbar.make(findViewById(R.id.btn_generate), "已复制: " + text, Snackbar.LENGTH_LONG).show();
        }
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
