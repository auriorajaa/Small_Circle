package com.org.smallcircle.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.org.smallcircle.R;
import com.org.smallcircle.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private ShimmerFrameLayout shimmerLayout;
    private TextView splashText;
    private LottieAnimationView backgroundAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize views
        shimmerLayout = findViewById(R.id.shimmerLayout);
        splashText = findViewById(R.id.splashText);
        backgroundAnimation = findViewById(R.id.backgroundAnimation);

        // Start shimmer effect
        shimmerLayout.startShimmer();

        // Create animation sequence
        AnimatorSet animatorSet = new AnimatorSet();

        // Logo fade in and scale
        ObjectAnimator logoFade = ObjectAnimator.ofFloat(splashText, "alpha", 0f, 1f);
        logoFade.setDuration(1000);
        logoFade.setStartDelay(500);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(splashText, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(splashText, "scaleY", 0.8f, 1f);

        // Play animations sequentially
        animatorSet.play(logoFade).with(scaleX).with(scaleY);

        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();

        // Navigate to main activity after delay
        new Handler().postDelayed(() -> {
            shimmerLayout.stopShimmer();
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, 3000); // 3 seconds delay
    }
}
