package com.org.smallcircle.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.org.smallcircle.utils.Utils;
import com.org.smallcircle.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    private static final String TAG = "FORGOT_PASS_TAG";

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    private String email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

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

        // Initialize real-time email validation
        setupEmailValidationWatcher();
    }

    private void setupEmailValidationWatcher() {
        binding.emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                email = s.toString().trim();

                // Check if email is valid
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Utils.setErrorState(binding.emailTextInputLayout, null); // Clear error if valid
                } else {
                    Utils.setErrorState(binding.emailTextInputLayout, "Invalid Email Address!"); // Set error if invalid
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void validateData() {
        Log.d(TAG, "validateData: ");

        // Ambil teks dari EditText
        email = binding.emailEditText.getText().toString().trim();

        boolean isValid = true;

        Log.d(TAG, "validateData: email: " + email);

        // Validate email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Utils.setErrorState(binding.emailTextInputLayout, "Invalid Email Address!");
            binding.emailEditText.requestFocus();
            isValid = false;
        } else {
            Utils.setErrorState(binding.emailTextInputLayout, null);
        }

        // Proceed to send reset password email if valid
        if (isValid) {
            sendPasswordRecoveryInstructions();
        }
    }

    private void sendPasswordRecoveryInstructions() {
        Log.d(TAG, "sendPasswordRecoveryInstructions: ");

        progressDialog.setMessage("Sending password recovery instructions to " + email);
        progressDialog.show();

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Utils.toast(ForgotPasswordActivity.this, "Password recovery instructions have been sent to " + email);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);

                        progressDialog.dismiss();
                        Utils.toast(ForgotPasswordActivity.this, "Failed to send, due to " + e.getMessage());
                    }
                });
    }
}
