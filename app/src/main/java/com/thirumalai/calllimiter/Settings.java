package com.thirumalai.calllimiter;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.materialswitch.MaterialSwitch;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Map;

public class Settings extends AppCompatActivity {
    private LinearLayout layoutTheme, githubIssues, permissions, about, timeLimitForAllNumbers, bufferLayout;
    private TextView selectedThemeText, bufferValueText, timeLimit;
    private ImageView backBtn;
    private SeekBar bufferBar;
    private MaterialSwitch switchBtn, bufferSwitchBtn;
    private boolean isChecked = false, isBufferEnabled = true;
    private final int[] BUFFER_VALUES = {10, 20, 30, 60, 120, 180, 240, 300};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        PreferenceHelper.init(this);

        selectedThemeText = findViewById(R.id.theme_text);
        backBtn = findViewById(R.id.back_btn);
        layoutTheme = findViewById(R.id.theme_layout);
        githubIssues = findViewById(R.id.github_issues);
        permissions = findViewById(R.id.permissions);
        about = findViewById(R.id.about_settings);
        bufferLayout = findViewById(R.id.buffer_layout);
        bufferSwitchBtn = findViewById(R.id.buffer_switch);
        bufferBar = findViewById(R.id.emergency_buffer_time_seek_bar);
        bufferValueText = findViewById(R.id.emergency_buffer_time_text);
        switchBtn = findViewById(R.id.limit_all_numbers_switch);
        timeLimitForAllNumbers = findViewById(R.id.time_limit_all_numbers_layout);
        timeLimit = findViewById(R.id.time_limit_all_numbers_text);

        isChecked = PreferenceHelper.getLimitForAllNumbersEnabled();
        int timeLimit1 = PreferenceHelper.getTimeLimitForAllNumbers();

        int hours = timeLimit1 / 3600;
        int minutes = (timeLimit1 % 3600) / 60;
        String hoursStr = "00", minutesStr = "00";
        if(hours < 10){
            hoursStr = "0" + hours;
        } else {
            hoursStr = Integer.toString(hours);
        }
        if(minutes < 10){
            minutesStr = "0" + minutes;
        } else {
            minutesStr = Integer.toString(minutes);
        }
        timeLimit.setText(hoursStr + ":" + minutesStr + ":" + "00");

        switchBtn.setChecked(isChecked);
        if(isChecked){
            timeLimitForAllNumbers.setVisibility(View.VISIBLE);
        } else {
            timeLimitForAllNumbers.setVisibility(View.GONE);
        }

        isBufferEnabled = PreferenceHelper.isBufferEnabled();
        bufferSwitchBtn.setChecked(isBufferEnabled);
        if(isBufferEnabled){
            bufferLayout.setVisibility(View.VISIBLE);
        } else {
            bufferLayout.setVisibility(View.GONE);
        }

        int bufferTime = PreferenceHelper.getBufferTime();
        bufferBar.setMax(BUFFER_VALUES.length - 1);
        int index = 0;
        for(int i = 0; i < BUFFER_VALUES.length; i++){
            if(BUFFER_VALUES[i] == bufferTime){
                index = i;
            }
        }

        selectedThemeText.setText(PreferenceHelper.getTheme());
        bufferBar.setProgress(index);
        bufferValueText.setText(formatBufferTime(bufferTime));
        layoutTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showThemeBottomSheet();
            }
        });

        githubIssues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Thiru-Malai/CallLimiter/issues"));
                startActivity(intent);
            }
        });

        permissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.this, Permissions.class);
                startActivity(intent);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        bufferBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int selectedValue = BUFFER_VALUES[progress];
                bufferValueText.setText(formatBufferTime(selectedValue));

                PreferenceHelper.saveBufferTime(selectedValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.this, About.class);
                startActivity(intent);
            }
        });

        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferenceHelper.updateLimitForAllNumbersEnabled(b);
                if(b){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(getApplicationContext(), CallMonitorService.class));
                        Log.d("Settings", "Started Foreground Service from Settings");
                    } else {
                        Log.d("Settings", "Staring Foreground Failed from Settings");
                    }
                    timeLimitForAllNumbers.setVisibility(View.VISIBLE);
                } else {
                    Map<String, ?> allEntries = PreferenceHelper.getAllContact();
                    if(allEntries.isEmpty()){
                        Intent stopForegroundService = new Intent(getApplicationContext(), CallMonitorService.class);
                        stopService(stopForegroundService);
                        Log.d("Settings", "Stopped Foreground Service from Settings");
                    }
                    timeLimitForAllNumbers.setVisibility(View.GONE);
                }
            }
        });

        timeLimit.setOnClickListener(view -> {
                TimerBottomSheet bottomSheet = new TimerBottomSheet(new TimerBottomSheet.OnTimeSelectedListener() {
                    @Override
                    public void onTimeSelected(int hours, int minutes) {
                        System.out.println(hours + " " + minutes);

                        String hoursStr = "00", minutesStr = "00";
                        if(hours < 10){
                            hoursStr = "0" + hours;
                        } else {
                            hoursStr = Integer.toString(hours);
                        }
                        if(minutes < 10){
                            minutesStr = "0" + minutes;
                        } else {
                            minutesStr = Integer.toString(minutes);
                        }
                        timeLimit.setText(hoursStr + ":" + minutesStr + ":" + "00");

                        int timeLimitInSeconds = (hours * 3600) + (minutes * 60);

                        PreferenceHelper.updateTimeLimitForAllNumbers(timeLimitInSeconds);
                    }

                    @Override
                    public void onTimerReset() { }
                });
                bottomSheet.show(getSupportFragmentManager(), "TimerBottomSheet");
        });

        bufferSwitchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferenceHelper.saveBufferEnabled(b);
                if(b){
                    bufferLayout.setVisibility(View.VISIBLE);
                } else {
                    bufferLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void showThemeBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.theme_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);

        String selectedTheme = PreferenceHelper.getTheme();

        LinearLayout optionSystem = sheetView.findViewById(R.id.option_system);
        LinearLayout optionLight = sheetView.findViewById(R.id.option_light);
        LinearLayout optionDark = sheetView.findViewById(R.id.option_dark);

        RadioButton systemRadioBtn = sheetView.findViewById(R.id.system_radio_btn);
        RadioButton lightRadioBtn = sheetView.findViewById(R.id.light_radio_btn);
        RadioButton darkRadioBtn = sheetView.findViewById(R.id.dark_radio_btn);

        switch (selectedTheme){
            case "System":
                systemRadioBtn.setChecked(true);
            break;
            case "Light":
                lightRadioBtn.setChecked(true);
            break;
            case "Dark":
                darkRadioBtn.setChecked(true);
            break;
        }

        optionSystem.setOnClickListener(v -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            PreferenceHelper.saveTheme("System");
            selectedThemeText.setText("System");
            bottomSheetDialog.dismiss();
        });

        optionLight.setOnClickListener(v -> {
            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if(nightModeFlags != Configuration.UI_MODE_NIGHT_NO){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            PreferenceHelper.saveTheme("Light");
            selectedThemeText.setText("Light");
            bottomSheetDialog.dismiss();
        });

        optionDark.setOnClickListener(v -> {
            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if(nightModeFlags != Configuration.UI_MODE_NIGHT_YES){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            PreferenceHelper.saveTheme("Dark");
            selectedThemeText.setText("Dark");
            bottomSheetDialog.dismiss();
        });

        systemRadioBtn.setOnClickListener(v -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            PreferenceHelper.saveTheme("System");
            selectedThemeText.setText("System");
            bottomSheetDialog.dismiss();
        });

        lightRadioBtn.setOnClickListener(v -> {
            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if(nightModeFlags != Configuration.UI_MODE_NIGHT_NO){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            PreferenceHelper.saveTheme("Light");
            selectedThemeText.setText("Light");
            bottomSheetDialog.dismiss();
        });

        darkRadioBtn.setOnClickListener(v -> {
            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if(nightModeFlags != Configuration.UI_MODE_NIGHT_YES){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            PreferenceHelper.saveTheme("Dark");
            selectedThemeText.setText("Dark");
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private String formatBufferTime(int seconds) {
        if (seconds < 60) {
            return seconds + " s";
        } else {
            int minutes = seconds / 60;
            return minutes + " min";
        }
    }
}