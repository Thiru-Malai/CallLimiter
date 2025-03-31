package com.thirumalai.calllimiter;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Button setLimit;
    private Button selectFromContacts;
    private int selectedHour, selectedMinute;
    private TextInputEditText phoneNumberField;
    boolean isPhoneAvailable = false, isTimeAvailable = false;
    private SharedPreferences sharedPreferences;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button timeLimitButton = findViewById(R.id.set_time_limit_button);
        setLimit = findViewById(R.id.set_limit_button);
        selectFromContacts = findViewById(R.id.select_contact_button);
        phoneNumberField = findViewById(R.id.phone_number_input);

        setLimit.setEnabled(false);

        // Fetch Stored Values
        updateSavedLimitsUI();

        // Request necessary permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.ANSWER_PHONE_CALLS,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.READ_CALL_LOG
            }, 1);
        }

        // Start foreground service
        startForegroundService(new Intent(this, CallMonitorService.class));

        // Add listener to phone number field
        phoneNumberField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isPhoneAvailable = count != 0;
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        timeLimitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimerBottomSheet bottomSheet = new TimerBottomSheet(new TimerBottomSheet.OnTimeSelectedListener() {
                    @Override
                    public void onTimeSelected(int hours, int minutes) {
                        System.out.println(hours + " " + minutes);
                        selectedHour = hours;
                        selectedMinute = minutes;
                        setLimit.setText("SET LIMIT - " + hours + " hrs " + minutes + " mins");
                        Toast.makeText(MainActivity.this, hours + " " + minutes, Toast.LENGTH_SHORT).show();
                        if(isPhoneAvailable){
                            setLimit.setEnabled(true);
                        }
                    }

                    @Override
                    public void onTimerReset() {
                          isTimeAvailable = false;
                    }
                });
                bottomSheet.show(getSupportFragmentManager(), "TimerBottomSheet");
            }
        });

        setLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber =  Objects.requireNonNull(phoneNumberField.getText()).toString().trim();

                int totalSeconds = (selectedHour * 3600) + (selectedMinute * 60);
                sharedPreferences = getSharedPreferences("time_limits", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // Save the phone number as the key and time limit as the value
                editor.putInt(phoneNumber, totalSeconds);
                editor.apply();

                isPhoneAvailable = false;
                phoneNumberField.setText(null);
                setLimit.setText("SET LIMIT");
                setLimit.setEnabled(false);

                // Refresh UI
                updateSavedLimitsUI();
            }
        });
    }

    private void updateSavedLimitsUI() {
        LinearLayout savedLimitsLayout = findViewById(R.id.saved_limits_layout);
        savedLimitsLayout.removeAllViews(); // Clear previous entries

        SharedPreferences sharedPreferences = getSharedPreferences("time_limits", MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String phoneNumber = entry.getKey();
            int totalSeconds = (int) entry.getValue();
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;

            // Create a horizontal layout for each entry
            LinearLayout entryLayout = new LinearLayout(this);
            entryLayout.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams entryLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,  // Width
                    LinearLayout.LayoutParams.WRAP_CONTENT   // Height
            );
            entryLayoutParams.setMargins(0, 0, 0, 60);
            entryLayout.setLayoutParams(entryLayoutParams);

            // Display phone number and time limit
            LinearLayout valueLayout = new LinearLayout(this);
            valueLayout.setOrientation(LinearLayout.VERTICAL);
            valueLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.8F));

            TextView number = new TextView(this);
            number.setText(phoneNumber);
            number.setTextSize(20);
            number.setTextColor(Color.WHITE);
            number.setTypeface(Typeface.DEFAULT_BOLD);

            TextView time = new TextView(this);
            time.setText(hours + " hrs " + minutes + " mins");
            time.setTextSize(16);

            valueLayout.addView(number);
            valueLayout.addView(time);

            // Create a delete button
            MaterialButton deleteButton = new MaterialButton(this);
            deleteButton.setId(View.generateViewId());
            deleteButton.setTextSize(16);
            deleteButton.setCornerRadius(8);
            deleteButton.setIconPadding(0);
            deleteButton.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
            deleteButton.setOnClickListener(v -> deleteTimeLimit(phoneNumber));
            deleteButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.2F));

            Drawable icon = ContextCompat.getDrawable(this, R.drawable.delete_24px);
            deleteButton.setIcon(icon);

            // Add views to layout
            entryLayout.addView(valueLayout);
            entryLayout.addView(deleteButton);
            savedLimitsLayout.addView(entryLayout);
        }
    }

    private void deleteTimeLimit(String phoneNumber) {
        SharedPreferences sharedPreferences = getSharedPreferences("time_limits", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(phoneNumber);
        editor.apply();

        // Refresh UI
        updateSavedLimitsUI();
    }
}
