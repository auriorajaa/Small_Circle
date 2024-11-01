package com.org.smallcircle.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.org.smallcircle.utils.Utils;
import com.org.smallcircle.databinding.ActivityChangePasswordBinding;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;

    private static final String TAG = "CHANGE_PASS_TAG";

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private ProgressDialog progressDialog;

    private String currentPassword = "";
    private String newPassword = "";
    private String confirmNewPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        // Check provider authentication
        boolean isEmailProvider = false;
        for (UserInfo userInfo : firebaseUser.getProviderData()) {
            if ("password".equals(userInfo.getProviderId())) {
                isEmailProvider = true;
                break;
            }
        }

        // If not email provider, show message and finish activity
        if (!isEmailProvider) {
            Utils.toast(ChangePasswordActivity.this, "Password change is only available for email/password login");
            finish();
            return;
        }

        binding.toolbarBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        // Real-time validation
        addTextWatchers();
    }

    private void addTextWatchers() {
        binding.newPasswordEditText.addTextChangedListener(new PasswordTextWatcher());
        binding.confirmPasswordEditText.addTextChangedListener(new PasswordTextWatcher());
    }

    private class PasswordTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            validatePasswords();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    private void validatePasswords() {
        newPassword = binding.newPasswordEditText.getText().toString();
        confirmNewPassword = binding.confirmPasswordEditText.getText().toString();

        if (newPassword.equals(confirmNewPassword)) {
            Utils.setErrorState(binding.confirmPasswordLayout, null);
        } else {
            Utils.setErrorState(binding.confirmPasswordLayout, "Password doesn't match!");
        }
    }

    private void validateData() {
        boolean isValid = true;

        Log.d(TAG, "validateData: ");

        currentPassword = binding.currentPasswordEditText.getText().toString();
        newPassword = binding.newPasswordEditText.getText().toString();
        confirmNewPassword = binding.confirmPasswordEditText.getText().toString();

        if (currentPassword.isEmpty()) {
            Utils.setErrorState(binding.currentPasswordLayout, "Please enter your current password!");
            binding.currentPasswordEditText.requestFocus();
            isValid = false;
        } else {
            Utils.setErrorState(binding.currentPasswordLayout, null);
        }

        if (newPassword.isEmpty()) {
            Utils.setErrorState(binding.newPasswordLayout, "Please enter your new password!");
            binding.newPasswordEditText.requestFocus();
            isValid = false;
        } else {
            Utils.setErrorState(binding.newPasswordLayout, null);
        }

        if (confirmNewPassword.isEmpty()) {
            Utils.setErrorState(binding.confirmPasswordLayout, "Please enter to confirm new password!");
            binding.confirmPasswordEditText.requestFocus();
            isValid = false;
        } else {
            Utils.setErrorState(binding.confirmPasswordLayout, null);
        }

        if (isValid && newPassword.equals(confirmNewPassword)) {
            authenticatedUserForUpdatePassword();
        }
    }

    private void authenticatedUserForUpdatePassword() {
        Log.d(TAG, "authenticatedUserForUpdatePassword: ");
        progressDialog.setMessage("Authenticating user...");
        progressDialog.show();

        if (currentPassword.isEmpty()) {
            updatePassword();
        } else {
            AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);
            firebaseUser.reauthenticate(authCredential)
                    .addOnSuccessListener(unused -> updatePassword())
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(ChangePasswordActivity.this, "Failed, due to: " + e.getMessage());
                    });
        }
    }

    private void updatePassword() {
        Log.d(TAG, "updatePassword: ");

        progressDialog.setMessage("Updating password");
        progressDialog.show();

        firebaseUser.updatePassword(newPassword)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Utils.toast(ChangePasswordActivity.this, "Password successfully updated!");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "onFailure: ", e);
                    progressDialog.dismiss();
                    Utils.toast(ChangePasswordActivity.this, "Failed, due to: " + e.getMessage());
                });
    }
}
