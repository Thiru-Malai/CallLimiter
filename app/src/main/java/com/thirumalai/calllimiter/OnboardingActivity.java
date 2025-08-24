package com.thirumalai.calllimiter;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private Button nextButton, skipButton;
    private OnboardingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        PreferenceHelper.init(this);

        viewPager = findViewById(R.id.viewPager);
        nextButton = findViewById(R.id.btnNext);
        skipButton = findViewById(R.id.btnSkip);

        adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);

        nextButton.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(pos + 1);
            } else {
                setOnboardingCompleted();
                startMain();
            }
        });

        skipButton.setOnClickListener(v -> {
            setOnboardingCompleted();
            startMain();
        });
    }

    private void setOnboardingCompleted() {
        PreferenceHelper.setOnboardingCompleted();
    }

    private void startMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
