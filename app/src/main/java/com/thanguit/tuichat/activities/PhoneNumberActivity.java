package com.thanguit.tuichat.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.thanguit.tuichat.R;
import com.thanguit.tuichat.animations.AnimationScale;
import com.thanguit.tuichat.databinding.ActivityPhoneNumberBinding;
import com.thanguit.tuichat.utils.OpenSoftKeyboard;

public class PhoneNumberActivity extends AppCompatActivity {
    private ActivityPhoneNumberBinding activityPhoneNumberBinding;
    private static final String TAG = "PhoneNumberActivity";

    private AnimationScale animationScale;
    private OpenSoftKeyboard openSoftKeyboard;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int startColor = getWindow().getStatusBarColor();
            int endColor = ContextCompat.getColor(this, R.color.color_main_2);
            ObjectAnimator.ofArgb(getWindow(), "statusBarColor", startColor, endColor).start();
        }
        activityPhoneNumberBinding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(activityPhoneNumberBinding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        animationScale = AnimationScale.getInstance();
        openSoftKeyboard = OpenSoftKeyboard.getInstance();

        initializeViews();
        listeners();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initializeViews() {
        activityPhoneNumberBinding.ccpCountryCodePicker.setDefaultCountryUsingNameCode("VN");
        activityPhoneNumberBinding.ccpCountryCodePicker.resetToDefaultCountry();

        activityPhoneNumberBinding.edtPNumber.requestFocus();
    }

    private void listeners() {
        animationScale.eventButton(this, activityPhoneNumberBinding.btnPhoneNumber);
        activityPhoneNumberBinding.btnPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String countryCode = activityPhoneNumberBinding.ccpCountryCodePicker.getSelectedCountryCodeWithPlus().trim();
                String phoneNumber = activityPhoneNumberBinding.edtPNumber.getText().toString().trim();
                String yourPhoneNumber = countryCode + phoneNumber;

                if (phoneNumber.isEmpty()) {
                    activityPhoneNumberBinding.edtPNumber.setError(getString(R.string.edtPNumberError));
                    openSoftKeyboard.openSoftKeyboard(PhoneNumberActivity.this, activityPhoneNumberBinding.edtPNumber);
                } else {
                    Log.d(TAG, "Your Phone Number: " + yourPhoneNumber.trim());

                    Intent intent = new Intent(PhoneNumberActivity.this, OTPActivity.class);
                    intent.putExtra("PHONE_NUMBER", yourPhoneNumber.trim());
                    startActivity(intent);
                }
            }
        });

    }
}