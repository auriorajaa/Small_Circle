package com.org.smallcircle.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.org.smallcircle.utils.Utils;
import com.org.smallcircle.databinding.ActivityLoginEmailBinding;

public class LoginEmailActivity extends AppCompatActivity {

    private ActivityLoginEmailBinding binding;

    private static final String TAG = "LOGIN_TAG";

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        // Back button is pressed
        binding.toolbarBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Register text is pressed
        binding.noAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginEmailActivity.this, RegisterEmailActivity.class));
            }
        });

        // Setup real-time validation
        setupEmailPasswordValidation();

        binding.forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginEmailActivity.this, ForgotPasswordActivity.class));
            }
        });

        // Login button is pressed
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void setupEmailPasswordValidation() {
        // Real-time validation for email
        binding.emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    Utils.setErrorState(binding.emailTextInputLayout, null); // Clear error if valid
                } else {
                    Utils.setErrorState(binding.emailTextInputLayout, "Invalid Email Address!"); // Show error if invalid
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Real-time validation for password
        binding.passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    Utils.setErrorState(binding.passwordTextInputLayout, null); // Clear error if password not empty
                } else {
                    Utils.setErrorState(binding.passwordTextInputLayout, "Please Enter a Password!"); // Show error if empty
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void validateData() {
        email = binding.emailEditText.getText().toString().trim();
        password = binding.passwordEditText.getText().toString();

        boolean isValid = true;

        // Validate email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Utils.setErrorState(binding.emailTextInputLayout, "Invalid Email Address!");
            binding.emailEditText.requestFocus();
            isValid = false;
        } else {
            Utils.setErrorState(binding.emailTextInputLayout, null);
        }

        // Validate password
        if (password.isEmpty()) {
            Utils.setErrorState(binding.passwordTextInputLayout, "Please Enter a Password!");
            binding.passwordEditText.requestFocus();
            isValid = false;
        } else {
            Utils.setErrorState(binding.passwordTextInputLayout, null);
        }

        // Proceed to login if valid
        if (isValid) {
            loginUser();
        }
    }

    private void loginUser() {
        // Show progress dialog
        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        // Authenticate with Firebase
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "onSuccess: Logged In...");
                        progressDialog.dismiss();

                        startActivity(new Intent(LoginEmailActivity.this, MainActivity.class));

                        // Finish all activities from back stack
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        Utils.toast(LoginEmailActivity.this, "Login failed, due to " + e.getMessage());
                        progressDialog.dismiss();
                    }
                });
    }
}
