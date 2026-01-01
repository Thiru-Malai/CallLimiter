package com.thirumalai.calllimiter.UI;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.thirumalai.calllimiter.R;

public class About extends AppCompatActivity {
    TextView version;
    ImageView back_btn;
    CardView sponsor, sourceCode, changeLog, termsAndConditions, privacyPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.about_layout), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        version = findViewById(R.id.version);
        sponsor = findViewById(R.id.sponsor);
        sourceCode = findViewById(R.id.source_code);
        termsAndConditions = findViewById(R.id.terms_conditions_about);
        privacyPolicy = findViewById(R.id.privacy_policy_about);
        changeLog = findViewById(R.id.change_log);
        back_btn = findViewById(R.id.back_btn_about);

        PackageInfo packageInfo;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            version.setText(String.format("Version %s", versionName));
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        sponsor.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.buymeacoffee.com/thirumalaikg"));
            startActivity(intent);            });

        sourceCode.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Thiru-Malai/CallLimiter"));
            startActivity(intent);
        });

        changeLog.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Thiru-Malai/CallLimiter/releases"));
            startActivity(intent);
        });

        termsAndConditions.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Thiru-Malai/CallLimiter/blob/master/TermsAndConditions.md"));
            startActivity(intent);
        });

        privacyPolicy.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Thiru-Malai/CallLimiter/blob/master/PrivacyPolicy.md"));
            startActivity(intent);
        });

        back_btn.setOnClickListener(view -> {
            Intent intent = new Intent(About.this, Settings.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

    }
}