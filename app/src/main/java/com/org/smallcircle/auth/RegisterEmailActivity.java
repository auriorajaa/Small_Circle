package com.org.smallcircle.auth;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.org.smallcircle.activity.MainActivity;
import com.org.smallcircle.utils.Utils;
import com.org.smallcircle.databinding.ActivityRegisterEmailBinding;

import java.util.HashMap;

public class RegisterEmailActivity extends AppCompatActivity {

    private ActivityRegisterEmailBinding binding;
    private static final String TAG = "REGISTER_TAG";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private String email, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        // Call to setup real-time email validation
        setupEmailValidation();

        // Call to setup real-time password validation
        setupPasswordValidation();

        // When back button is pressed
        binding.toolbarBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // When already have an account text is pressed
        binding.alreadyHaveAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterEmailActivity.this, LoginEmailActivity.class));
            }
        });

        binding.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    // Set up real-time email validation
    private void setupEmailValidation() {
        binding.emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Validate the email as the user types
                if (Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    Utils.setErrorState(binding.emailTextInputLayout, null); // Clear error if valid
                } else {
                    Utils.setErrorState(binding.emailTextInputLayout, "Invalid Email Address!"); // Set error if invalid
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // Set up real-time password validation
    private void setupPasswordValidation() {
        binding.passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error if password field is empty
                if (s.length() == 0) {
                    Utils.setErrorState(binding.passwordTextInputLayout, null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Similar logic for confirm password field
        binding.confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    Utils.setErrorState(binding.confirmPasswordTextInputLayout, null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }


    private void validateData() {
        boolean isValid = true;

        email = binding.emailEditText.getText().toString().trim();
        password = binding.passwordEditText.getText().toString();
        confirmPassword = binding.confirmPasswordEditText.getText().toString();

        Log.d(TAG, "validateData: email: " + email);
        Log.d(TAG, "validateData: password: " + password);
        Log.d(TAG, "validateData: confirmPassword: " + confirmPassword);

        // Check email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Utils.setErrorState(binding.emailTextInputLayout, "Invalid Email Address!");
            binding.emailEditText.requestFocus();
            isValid = false;
        } else {
            Utils.setErrorState(binding.emailTextInputLayout, null);
        }

        // Check password
        if (password.isEmpty()) {
            Utils.setErrorState(binding.passwordTextInputLayout, "Please Enter a Password!");
            binding.passwordEditText.requestFocus();
            isValid = false;
        } else {
            Utils.setErrorState(binding.passwordTextInputLayout, null);
        }

        // Check confirm password
        if (!password.equals(confirmPassword)) {
            Utils.setErrorState(binding.confirmPasswordTextInputLayout, "Password Doesn't Match!");
            binding.confirmPasswordEditText.requestFocus();
            isValid = false;
        } else {
            Utils.setErrorState(binding.confirmPasswordTextInputLayout, null);
        }

        // If all fields are valid, register the user
        if (isValid) {
            registerUser();
        }
    }

    private void registerUser() {
        progressDialog.setMessage("Creating Account");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "onSuccess: Register success");
                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        Utils.toast(RegisterEmailActivity.this, "Registration failed, due to " + e.getMessage());
                        progressDialog.dismiss();
                    }
                });
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Saving User Info");

        long timestamp = Utils.getTimestamp();
        String registerUserEmail = firebaseAuth.getCurrentUser().getEmail();
        String registerUserUid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "");
        hashMap.put("phoneCode", "");
        hashMap.put("phoneNumber", "");
        hashMap.put("profileImageUrl", "");
        hashMap.put("dob", "");
        hashMap.put("userType", "Email"); // Possible values: Email,Phone,Google
        hashMap.put("typingTo", "");
        hashMap.put("timestamp", timestamp);
        hashMap.put("onlineStatus", true);
        hashMap.put("email", registerUserEmail);
        hashMap.put("uid", registerUserUid);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(registerUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Info saved...");
                        progressDialog.dismiss();

                        startActivity(new Intent(RegisterEmailActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(RegisterEmailActivity.this, "Failed to register, due to " + e.getMessage());
                    }
                });
    }
}
