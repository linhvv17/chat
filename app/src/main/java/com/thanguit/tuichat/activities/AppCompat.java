package com.thanguit.tuichat.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.thanguit.tuichat.database.DataLocalManager;
import com.thanguit.tuichat.utils.SettingLanguage;

public class AppCompat extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DataLocalManager.init(this);

        if (DataLocalManager.getTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        SettingLanguage.getInstance().changeLanguage(this, DataLocalManager.getLanguage());
    }
}
