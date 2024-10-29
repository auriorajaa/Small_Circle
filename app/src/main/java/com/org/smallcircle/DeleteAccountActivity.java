package com.org.smallcircle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.org.smallcircle.databinding.ActivityDeleteAccountBinding;

public class DeleteAccountActivity extends AppCompatActivity {

    private ActivityDeleteAccountBinding binding;
    private static final String TAG = "DELETE_ACCOUNT_TAG";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeleteAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setting up the default uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Uncaught exception: ", throwable);
            // Optionally, restart the app or finish the current activity
            finish();
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        binding.toolbarBackButton.setOnClickListener(v -> onBackPressed());
        binding.deleteAccountButton.setOnClickListener(v -> deleteAccount());
    }

    private void deleteAccount() {
        try {
            if (firebaseUser == null) {
                Log.e(TAG, "User is not logged in.");
                Utils.toast(this, "User not logged in.");
                return; // Prevent further actions if user is null
            }

            Log.d(TAG, "Attempting to delete user account");
            progressDialog.setMessage("Deleting user account");
            progressDialog.show();

            String myUid = firebaseUser.getUid();

            firebaseUser.delete()
                    .addOnSuccessListener(unused -> {
                        Log.d(TAG, "Account deleted successfully");
                        deleteUserAds(myUid);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to delete account", e);
                        progressDialog.dismiss();
                        Utils.toast(DeleteAccountActivity.this, e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteAccount: ", e);
            progressDialog.dismiss();
            Utils.toast(this, "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void deleteUserAds(String myUid) {
        try {
            Log.d(TAG, "Deleting user ads for UID: " + myUid);
            progressDialog.setMessage("Deleting user ads");
            DatabaseReference refUserAds = FirebaseDatabase.getInstance().getReference("Ads");

            refUserAds.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Log.d(TAG, "Removing ad: " + ds.getKey());
                        ds.getRef().removeValue();
                    }
                    deleteUserData(myUid); // Call next step after ads are deleted
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error deleting ads: ", error.toException());
                    progressDialog.dismiss();
                    Utils.toast(DeleteAccountActivity.this, error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteUserAds: ", e);
            progressDialog.dismiss();
            Utils.toast(this, "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void deleteUserData(String myUid) {
        try {
            Log.d(TAG, "Deleting user data for UID: " + myUid);
            progressDialog.setMessage("Deleting user data");
            DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("Users");

            refUsers.child(myUid).removeValue()
                    .addOnSuccessListener(unused -> {
                        Log.d(TAG, "User data deleted successfully");
                        startSplashActivity();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to delete user data", e);
                        progressDialog.dismiss();
                        Utils.toast(DeleteAccountActivity.this, e.getMessage());
                        startSplashActivity(); // Close the app even if user data deletion fails
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteUserData: ", e);
            progressDialog.dismiss();
            Utils.toast(this, "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void startSplashActivity() {
        Log.d(TAG, "Starting SplashActivity");
        progressDialog.dismiss(); // Dismiss the dialog before starting new activity
        startActivity(new Intent(this, SplashActivity.class));
        finishAffinity(); // Close all activities and exit app
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
