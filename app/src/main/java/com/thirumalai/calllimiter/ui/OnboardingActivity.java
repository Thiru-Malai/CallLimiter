package com.thirumalai.calllimiter.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.thirumalai.calllimiter.MainActivity;
import com.thirumalai.calllimiter.Fragment.OnboardingAdapter;
import com.thirumalai.calllimiter.data.PreferenceHelper;
import com.thirumalai.calllimiter.R;

public class OnboardingActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private Button nextButton, skipButton, readTermsAndConditions;
    private OnboardingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        PreferenceHelper.init(this);

        boolean termsAndConditionsRead = PreferenceHelper.getStatusTermsAndConditions();

        viewPager = findViewById(R.id.viewPager);
        nextButton = findViewById(R.id.btnNext);
        skipButton = findViewById(R.id.btnSkip);
        readTermsAndConditions = findViewById(R.id.read_terms_conditions);

        if(!termsAndConditionsRead){
            readTermsAndConditions.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.GONE);
        }
        else{
            readTermsAndConditions.setVisibility(View.GONE);
            nextButton.setVisibility(View.VISIBLE);
        }

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

        readTermsAndConditions.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Thiru-Malai/CallLimiter/blob/master/TermsAndConditions.md"));
            startActivity(intent);
            readTermsAndConditions.setVisibility(View.GONE);
            nextButton.setVisibility(View.VISIBLE);
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
