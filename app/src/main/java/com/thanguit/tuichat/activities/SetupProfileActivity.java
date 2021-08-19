package com.thanguit.tuichat.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thanguit.tuichat.R;
import com.thanguit.tuichat.animations.AnimationScale;
import com.thanguit.tuichat.databinding.ActivitySetupProfileBinding;
import com.thanguit.tuichat.models.User;
import com.thanguit.tuichat.utils.LoadingDialog;
import com.thanguit.tuichat.utils.MyToast;
import com.thanguit.tuichat.utils.OpenSoftKeyboard;

public class SetupProfileActivity extends AppCompatActivity {
    private ActivitySetupProfileBinding activitySetupProfileBinding;
    private static final String TAG = "SetupProfileActivity";

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;

    private static final String USER_AVATAR_STORAGE = "USER_AVATAR";
    private static final String USER_DATABASE = "users";

    private Uri selectImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySetupProfileBinding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(activitySetupProfileBinding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        initializeViews();
        listeners();
    }

    private void initializeViews() {
    }

    private void listeners() {
        AnimationScale.getInstance().eventCircleImageView(this, activitySetupProfileBinding.civUserAvatar);
        activitySetupProfileBinding.civUserAvatar.setOnClickListener(view -> activityResultLauncher.launch("image/*"));

        AnimationScale.getInstance().eventButton(this, activitySetupProfileBinding.btnSetupProfile);
        activitySetupProfileBinding.btnSetupProfile.setOnClickListener(view -> {
            if (activitySetupProfileBinding.edtSetupProfileName.getText().toString().trim().isEmpty()) {
                activitySetupProfileBinding.edtSetupProfileName.setError(getString(R.string.edtSetupProfileNameError));
                OpenSoftKeyboard.getInstance().openSoftKeyboard(this, activitySetupProfileBinding.edtSetupProfileName);
            } else if (selectImg == null) {
                MyToast.makeText(this, MyToast.WARNING, getString(R.string.toast4), MyToast.SHORT).show();
            } else {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    LoadingDialog.getInstance().startLoading(this, false);

                    StorageReference storageReference = firebaseStorage.getReference().child(USER_AVATAR_STORAGE.trim()).child(firebaseUser.getUid());
                    storageReference.putFile(selectImg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String uid = firebaseUser.getUid();
                                        String name = activitySetupProfileBinding.edtSetupProfileName.getText().toString().trim();
                                        String phoneNumber = firebaseUser.getPhoneNumber();
                                        String avatar = uri.toString().trim();

                                        User user = new User(uid, name, phoneNumber, avatar);
                                        firebaseDatabase.getReference()
                                                .child(USER_DATABASE.trim())
                                                .child(uid)
                                                .setValue(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        LoadingDialog.getInstance().cancelLoading();

                                                        Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null) {
                        selectImg = result;
                        activitySetupProfileBinding.civUserAvatar.setImageURI(result);
                    }
                }
            });
}