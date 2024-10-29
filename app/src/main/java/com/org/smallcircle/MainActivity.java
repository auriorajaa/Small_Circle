package com.org.smallcircle;

import android.content.Intent;
import android.os.Bundle;

import android.view.MenuItem;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.org.smallcircle.databinding.ActivityMainBinding;
import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get instance of Firebase for authorization
        firebaseAuth = FirebaseAuth.getInstance();

        // Check if user not logged in, open the Login options
        if (firebaseAuth.getCurrentUser() == null) {
            // Start Login options
            startLoginOptions();
        }

        showHomeFragment();

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.menu_home) {
                    // When Clicked, Start Home Fragment
                    showHomeFragment();

                    return true;
                } else if (itemId == R.id.menu_chat) {
                    // When Clicked, Start Chat Fragment
                    if (firebaseAuth.getCurrentUser() == null) {
                        Utils.toast(MainActivity.this, "Please Login to Start Conversation!");
                        startLoginOptions();

                        return false;
                    } else {
                        showChatFragment();

                        return true;
                    }
                } else if (itemId == R.id.menu_my_product) {
                    // When Clicked, Start My Product Fragment
                    if (firebaseAuth.getCurrentUser() == null) {
                        Utils.toast(MainActivity.this, "Please Login to See Your Products!");
                        startLoginOptions();

                        return false;
                    } else {
                        showMyProductFragment();

                        return true;
                    }
                } else if (itemId == R.id.menu_my_profile) {
                    // When Clicked, Start My Profile Fragment
                    if (firebaseAuth.getCurrentUser() == null) {
                        Utils.toast(MainActivity.this, "Please Login to Edit Your Profile!");
                        startLoginOptions();

                        return false;
                    } else {
                        showMyProfileFragment();

                        return true;
                    }
                } else {
                    return false;
                }
            }
        });
    }

    private void showHomeFragment() {
        // Change toolbar title
        binding.toolbarTitle.setText("Home");

        // Show HomeFragment
        HomeFragment fragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentFrameLayout.getId(), fragment, "HomeFragment");
        fragmentTransaction.commit();
    }

    private void showChatFragment() {
        binding.toolbarTitle.setText("Chat");

        // Show ChatFragment
        ChatFragment fragment = new ChatFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentFrameLayout.getId(), fragment, "ChatFragment");
        fragmentTransaction.commit();
    }

    private void showMyProductFragment() {
        binding.toolbarTitle.setText("My Product");

        // Show MyProductFragment
        MyProductFragment fragment = new MyProductFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentFrameLayout.getId(), fragment, "MyProductFragment");
        fragmentTransaction.commit();
    }

    private void showMyProfileFragment() {
        binding.toolbarTitle.setText("Profile");

        // Show MyProfileFragment
        MyProfileFragment fragment = new MyProfileFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentFrameLayout.getId(), fragment, "MyProfileFragment");
        fragmentTransaction.commit();
    }

    private void startLoginOptions() {
        startActivity(new Intent(this, LoginOptionsActivity.class));
    }
}