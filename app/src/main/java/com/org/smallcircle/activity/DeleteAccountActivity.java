package com.org.smallcircle.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.org.smallcircle.utils.Utils;
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
            finish();
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        binding.toolbarBackButton.setOnClickListener(v -> onBackPressed());
        binding.deleteAccountButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
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

            // Step 1: Delete profile image from Firebase Storage
            deleteProfileImage();

        } catch (Exception e) {
            Log.e(TAG, "Error in deleteAccount: ", e);
            progressDialog.dismiss();
            Utils.toast(this, "An unexpected error occurred: " + e.getMessage());
        }
    }

    // New method to delete profile image from Firebase Storage
    private void deleteProfileImage() {
        String myUid = firebaseUser.getUid();
        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("Users").child(myUid);

        refUsers.child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String profileImageUrl = snapshot.getValue(String.class);
                if (profileImageUrl != null) {
                    // Delete the image from Firebase Storage
                    StorageReference profileImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(profileImageUrl);
                    profileImageRef.delete()
                            .addOnSuccessListener(unused -> {
                                Log.d(TAG, "Profile image deleted successfully");
                                deleteUserProduct(myUid); // Move to delete user products after profile image deletion
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to delete profile image", e);
                                progressDialog.dismiss();
                                Utils.toast(DeleteAccountActivity.this, e.getMessage());
                            });
                } else {
                    // No profile image to delete, continue to delete user products
                    deleteUserProduct(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to get profile image URL", error.toException());
                progressDialog.dismiss();
                Utils.toast(DeleteAccountActivity.this, error.getMessage());
            }
        });
    }

    private void deleteUserProduct(String myUid) {
        try {
            Log.d(TAG, "Deleting user product for UID: " + myUid);
            progressDialog.setMessage("Deleting user product");
            DatabaseReference refUserProduct = FirebaseDatabase.getInstance().getReference("Product");

            refUserProduct.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Log.d(TAG, "Removing ad: " + ds.getKey());
                        ds.getRef().removeValue();
                    }
                    deleteUserData(myUid); // Call next step after product are deleted
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error deleting product: ", error.toException());
                    progressDialog.dismiss();
                    Utils.toast(DeleteAccountActivity.this, error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteUserProduct: ", e);
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

