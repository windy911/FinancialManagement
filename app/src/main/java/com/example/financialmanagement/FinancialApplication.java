package com.example.financialmanagement;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

public class FinancialApplication extends Application {

    private static final String BUGLY_APP_ID = "fad2bdb702";

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), BUGLY_APP_ID, true);
    }
}
