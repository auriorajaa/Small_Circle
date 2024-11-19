package com.org.smallcircle.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.org.smallcircle.activity.MainActivity;
import com.org.smallcircle.utils.Utils;
import com.org.smallcircle.databinding.ActivityLoginPhoneBinding;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginPhoneActivity extends AppCompatActivity {

    private ActivityLoginPhoneBinding binding;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    private PhoneAuthProvider.ForceResendingToken forceResendingToken;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String mVerificationId;

    private static final String TAG = "LOGIN_PHONE_TAG";

    private String phoneCode = "", phoneNumber = "", phoneNumberWithCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.phoneInputLayout.setVisibility(View.VISIBLE);
        binding.otpInputLayout.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        phoneLoginCallback();

        // Add real-time validation for phone number input
        setupPhoneNumberValidation();

        binding.toolbarBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.sendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        binding.resendOtpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode(forceResendingToken);
            }
        });

        binding.verifyOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = binding.otpEditText.getText().toString().trim();

                if (otp.isEmpty()) {
                    Utils.setErrorState(binding.otpTextLayout, "Please enter the OTP!");
                    binding.otpEditText.requestFocus();
                } else if (otp.length() < 6) {
                    Utils.setErrorState(binding.otpTextLayout, "OTP must be 6 characters long!");
                    binding.otpEditText.requestFocus();
                } else {
                    verifyPhoneNumberWithCode(mVerificationId, otp);
                }
            }
        });
    }

    // Function to set up real-time validation for phone number
    private void setupPhoneNumberValidation() {
        binding.phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error when the user starts typing and the phone number is valid
                if (!s.toString().trim().isEmpty()) {
                    Utils.setErrorState(binding.phoneTextLayout, null); // Clear the error
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void validateData() {

        phoneCode = binding.ccp.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneEditText.getText().toString().trim();
        phoneNumberWithCode = phoneCode + phoneNumber;

        Log.d(TAG, "validateData: phoneCode: " + phoneCode);
        Log.d(TAG, "validateData: phoneNumber: " + phoneNumber);
        Log.d(TAG, "validateData: phoneNumberWithCode: " + phoneNumberWithCode);

        if (phoneNumber.isEmpty()) {
            Utils.setErrorState(binding.phoneTextLayout, "Please Enter a Phone Number!");
            binding.phoneEditText.requestFocus();
        } else {
            startPhoneNumberVerification();
        }
    }

    private void startPhoneNumberVerification() {
        Log.d(TAG, "startPhoneNumberVerification: ");

        progressDialog.setMessage("Sending OTP to " + phoneNumberWithCode);
        progressDialog.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumberWithCode)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

        binding.phoneInputLayout.setVisibility(View.GONE);
        binding.otpInputLayout.setVisibility(View.VISIBLE);
    }

    private void resendVerificationCode(PhoneAuthProvider.ForceResendingToken token) {
        Log.d(TAG, "resendVerificationCode: ForceResendingToken: " + token);

        progressDialog.setMessage("Resending OTP to " + phoneNumberWithCode);
        progressDialog.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumberWithCode)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void phoneLoginCallback() {
        Log.d(TAG, "phoneLoginCallback: ");

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted: ");

                // Callback that will be called to invoke 2 situations:
                // 1 - Instant Verification. Some cases phone number can be instantly
                //     verified without needing to send verification code
                // 2 - Auto retrieval. Google play can automatically detect incoming
                //     verification SMS and perform without user confirmation on some devices

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e(TAG, "onVerificationFailed: ", e);

                // To invoke invalid format phone number
                progressDialog.dismiss();
                Utils.toast(LoginPhoneActivity.this, "Verification failed, due to " + e.getMessage());
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);

                // Asking user to enter code by combining it with verification ID
                mVerificationId = verificationId;
                forceResendingToken = token;

                progressDialog.dismiss();

                binding.phoneInputLayout.setVisibility(View.GONE);
                binding.otpInputLayout.setVisibility(View.VISIBLE);

                Utils.toast(LoginPhoneActivity.this, "OTP has been sent to " + phoneNumberWithCode);

                binding.loginDescription.setText(String.format("Please enter the verification code sent to %s", phoneNumberWithCode));
            }
        };
    }

    private void verifyPhoneNumberWithCode(String verificationId, String otp) {
        Log.d(TAG, "verifyPhoneNumberWithCode: verificationId: " + verificationId);
        Log.d(TAG, "verifyPhoneNumberWithCode: otp: " + otp);

        progressDialog.setMessage("Verifying OTP");
        progressDialog.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Log.d(TAG, "signInWithPhoneAuthCredential: ");
        progressDialog.setMessage("Logging In");

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "onSuccess: ");

                        if (authResult.getAdditionalUserInfo().isNewUser()) {
                            Log.d(TAG, "onSuccess: New user, Creating account...");

                            updateUserInfoDatabase();
                        } else {
                            Log.d(TAG, "onSuccess: Existing user, Logging in...");

                            startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
                            finishAffinity();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);

                        progressDialog.dismiss();
                        Utils.toast(LoginPhoneActivity.this, "Failed to login, due to " + e.getMessage());
                    }
                });
    }

    private void updateUserInfoDatabase() {
        Log.d(TAG, "updateUserInfoDatabase: ");

        progressDialog.setMessage("Saving user info");
        progressDialog.show();

        long timestamp = Utils.getTimestamp();
        String registerUserUid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "");
        hashMap.put("phoneCode", "" + phoneCode);
        hashMap.put("phoneNumber", "" + phoneNumber);
        hashMap.put("profileImageUrl", "");
        hashMap.put("dob", "");
        hashMap.put("userType", "Phone"); // Possible values: Email,Phone,Google
        hashMap.put("typingTo", "");
        hashMap.put("timestamp", timestamp);
        hashMap.put("onlineStatus", true);
        hashMap.put("email", "");
        hashMap.put("uid", registerUserUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(registerUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: ");

                        progressDialog.dismiss();
                        startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);

                        progressDialog.dismiss();
                        Utils.toast(LoginPhoneActivity.this, "Failed to register, due to " + e.getMessage());
                    }
                });
    }

}