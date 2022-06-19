package com.thanguit.tuichat.activities;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import com.thanguit.tuichat.R;
import com.thanguit.tuichat.adapters.ViewPagerAdapter;
import com.thanguit.tuichat.animations.AnimationScale;
import com.thanguit.tuichat.animations.ZoomOutPageTransformer;
import com.thanguit.tuichat.database.FirebaseManager;
import com.thanguit.tuichat.databinding.ActivityMainBinding;
import com.thanguit.tuichat.utils.MyToast;

public class MainActivity extends AppCompat {
    private ActivityMainBinding activityMainBinding;
    private static final String TAG = "MainActivity";
    private static final String STRINGEE = "Stringee";

    private FirebaseManager firebaseManager;

    private ViewPagerAdapter viewPagerAdapter;
    private FirebaseAuth firebaseAuth;

    private AnimationScale animationScale;

    private Toast myToast;
    private long BACK_PRESS_TIME = 0;

//    // Meizu M3 Note
//    private final String token1 = "eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS0JGRGZ3NDJhVWZTWUNvSThINTk4Zkd0aHJ3cTRlRVEtMTYzMTk4MDk4OSIsImlzcyI6IlNLQkZEZnc0MmFVZlNZQ29JOEg1OThmR3RocndxNGVFUSIsImV4cCI6MTYzNDU3Mjk4OSwidXNlcklkIjoiTWFwSktNNTNHSWJhWVJvQ1NsTFJzYUZHTmRyMiJ9.NTA4QSWtEuPVocj9x1X2WzgSjLayTvFj9cf3TudicnQ";
//
//    // Android Virtual Pixel 3a
//    private final String token2 = "eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS0JGRGZ3NDJhVWZTWUNvSThINTk4Zkd0aHJ3cTRlRVEtMTYzMTk4MTA0MCIsImlzcyI6IlNLQkZEZnc0MmFVZlNZQ29JOEg1OThmR3RocndxNGVFUSIsImV4cCI6MTYzNDU3MzA0MCwidXNlcklkIjoiZWZjVFNVcUNPV1hqNW9tckVNRzVZZEhLRG9WMiJ9.gZgFh-34CLlgpf7pMmvfT18eengMdNLeo8WY5_9rFB0";
//    public static StringeeClient stringeeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        firebaseManager = FirebaseManager.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        animationScale = AnimationScale.getInstance();

        initializeViews();
        listeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatusOnline();
//        recreate();
    }

    @Override
    protected void onDestroy() {
        setStatusOffline();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (BACK_PRESS_TIME + 2000 > System.currentTimeMillis()) {
            myToast.cancel();
            super.onBackPressed();
            return;
        } else {
            myToast = MyToast.makeText(MainActivity.this, MyToast.INFORMATION, getString(R.string.toast13), MyToast.SHORT);
            myToast.show();
        }
        BACK_PRESS_TIME = System.currentTimeMillis();
    }

    private void initializeViews() {
        animationScale.eventCircleImageView(this, activityMainBinding.civAvatar);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        activityMainBinding.vp2ViewPager2.setAdapter(viewPagerAdapter);
        activityMainBinding.vp2ViewPager2.setCurrentItem(0); // Set default fragment
//        activityMainBinding.vp2ViewPager2.setOffscreenPageLimit(2);
        activityMainBinding.vp2ViewPager2.setPageTransformer(new ZoomOutPageTransformer()); // Set animation change page

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String token) {
                    if (token != null && !token.isEmpty()) {
                        FirebaseManager.getInstance().setUserToken(currentUser.getUid(), token);
                    }
                }
            });

            firebaseManager.getUserAvatar(currentUser.getUid());
            firebaseManager.setReadUserAvatar(new FirebaseManager.GetUserAvatarListener() {
                @Override
                public void getUserAvatarListener(String avatar) {
                    Picasso.get()
                            .load(avatar)
                            .placeholder(R.drawable.ic_user_avatar)
                            .error(R.drawable.ic_user_avatar)
                            .into(activityMainBinding.civAvatar);
                }
            });

//            initAndConnectStringee();
        }
    }

//    private void initAndConnectStringee() {
//        stringeeClient = new StringeeClient(this);
//        stringeeClient.setConnectionListener(new StringeeConnectionListener() {
//            @Override
//            public void onConnectionConnected(StringeeClient stringeeClient, boolean b) {
//                Log.d(STRINGEE, "Connect successfully");
//            }
//
//            @Override
//            public void onConnectionDisconnected(StringeeClient stringeeClient, boolean b) {
//                Log.d(STRINGEE, "Disconnected Connect");
//            }
//
//            @Override
//            public void onIncomingCall(StringeeCall stringeeCall) {
//                Log.d(STRINGEE, "onIncommingCall");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (Common.isInCall) {
//                            stringeeCall.reject();
//                        } else {
//                            Common.callsMap.put(stringeeCall.getCallId(), stringeeCall);
//                            Intent intent = new Intent(MainActivity.this, InComingCallActivity.class);
//                            intent.putExtra("CALL_ID", stringeeCall.getCallId());
//                            startActivity(intent);
//                        }
//                    }
//                });
//            }
//
//            @Override
//            public void onIncomingCall2(StringeeCall2 stringeeCall2) {
////                runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
////                        if (Common.isInCall) {
////                            stringeeCall2.reject();
////                        } else {
////                            Common.calls2Map.put(stringeeCall2.getCallId(), stringeeCall2);
////                            Intent intent = new Intent(MainActivity.this, IncomingCall2Activity.class);
////                            intent.putExtra("call_id", stringeeCall2.getCallId());
////                            startActivity(intent);
////                        }
////                    }
////                });
//            }
//
//            @Override
//            public void onConnectionError(StringeeClient stringeeClient, StringeeError stringeeError) {
//                Log.d("Stringee", "StringeeClient fails to connect: " + stringeeError.getMessage());
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Common.reportMessage(MainActivity.this, "StringeeClient fails to connect: " + stringeeError.getMessage());
//                    }
//                });
//            }
//
//            @Override
//            public void onRequestNewToken(StringeeClient stringeeClient) {
//            }
//
//            @Override
//            public void onCustomMessage(String s, JSONObject jsonObject) {
//            }
//
//            @Override
//            public void onTopicMessage(String s, JSONObject jsonObject) {
//            }
//        });

//        stringeeClient.connect(token2); //Important
//    }

    private void listeners() {
        activityMainBinding.civAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });

        activityMainBinding.bnvNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menuActionChat) {
                    activityMainBinding.vp2ViewPager2.setCurrentItem(0);
                } else if (item.getItemId() == R.id.menuActionPeople) {
                    activityMainBinding.vp2ViewPager2.setCurrentItem(1);
                } else if (item.getItemId() == R.id.menuActionSetting) {
                    activityMainBinding.vp2ViewPager2.setCurrentItem(2);
                }
                return false;
            }
        });

        activityMainBinding.vp2ViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                switch (position) {
                    case 0: {
                        activityMainBinding.bnvNav.getMenu().findItem(R.id.menuActionChat).setChecked(true);
                        break;
                    }
                    case 1: {
                        activityMainBinding.bnvNav.getMenu().findItem(R.id.menuActionPeople).setChecked(true);
                        break;
                    }
                    case 2: {
                        activityMainBinding.bnvNav.getMenu().findItem(R.id.menuActionSetting).setChecked(true);
                        break;
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    private void setStatusOnline() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firebaseManager.setStatusOnline(currentUser.getUid());
        }
    }

    private void setStatusOffline() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firebaseManager.setStatusOffline(currentUser.getUid());
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }
}