package com.thirumalai.calllimiter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.ContactsContract;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.thirumalai.calllimiter.BottomSheets.TimerBottomSheet;
import com.thirumalai.calllimiter.Data.PreferenceHelper;
import com.thirumalai.calllimiter.Service.CallMonitorService;
import com.thirumalai.calllimiter.UI.OnboardingActivity;
import com.thirumalai.calllimiter.UI.Settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Button setLimit, selectFromContacts;
    private int selectedHour = -1, selectedMinute = -1;
    private TextInputEditText phoneNumberField, contactNameField;
    boolean isPhoneAvailable = false, isTimeAvailable = false;
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;
    private static final int PICK_CONTACT_REQUEST = 1;
    private ImageView settings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceHelper.init(this);

        boolean isFirstTimeLogin = PreferenceHelper.isFirstTimeLogin();
        String selectedTheme = PreferenceHelper.getTheme();
        setTheme(selectedTheme);
        if(isFirstTimeLogin) {
            PreferenceHelper.saveBufferTime(10);
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        setupStatusBarAppearance();

        Button timeLimitButton = findViewById(R.id.set_time_limit_button);
        setLimit = findViewById(R.id.set_limit_button);
        selectFromContacts = findViewById(R.id.select_contact_button);
        phoneNumberField = findViewById(R.id.phone_number_input);
        contactNameField = findViewById(R.id.contact_name);
        settings = findViewById(R.id.settings_button);
        // Check for last updated date change
        checkAndSetInitialDate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            requestNecessaryPermissions();
        }

        // Fetch Stored Values
        try {
            updateSavedLimitsUI("LoadOnCreate");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

//        Start foreground service
//        startForegroundService(new Intent(this, CallMonitorService.class));

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
                String contactName = Objects.requireNonNull(contactNameField.getText()).toString().trim();
                if(phoneNumber.isEmpty() || selectedHour == -1 || selectedMinute == -1){
                    Toast.makeText(MainActivity.this, "Please make sure phone number and time limit is set.", Toast.LENGTH_SHORT).show();
                    return;
                }
                int totalSeconds = (selectedHour * 3600) + (selectedMinute * 60);

                // Save the phone number as the key and time limit as the value
                JSONObject jsonObject = new JSONObject();
                try{
                    if(!contactName.isEmpty()) {
                        jsonObject.put("name", contactName);
                    }
                    jsonObject.put("limit", totalSeconds);
                    jsonObject.put("remaining_time", totalSeconds);
                    jsonObject.put("last_updated", getTodayDate());
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                PreferenceHelper.saveContact(phoneNumber, jsonObject.toString());

                isPhoneAvailable = false;
                phoneNumberField.setText(null);
                contactNameField.setText(null);
                setLimit.setText(R.string.set_limit);

                clearInputFocus();

                // Resetting time
                selectedHour = -1;
                selectedMinute = -1;

                // Refresh UI
                try {
                    updateSavedLimitsUI("SetLimit");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        selectFromContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT_REQUEST);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivity(intent);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void requestNecessaryPermissions() {
        // Request necessary permissions
        List<String> permissionsNeeded = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_CALL_LOG);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ANSWER_PHONE_CALLS);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ANSWER_PHONE_CALLS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    1);
        }
    }

    public void updateSavedLimitsUI(String action) throws JSONException {
        LinearLayout savedLimitsLayout = findViewById(R.id.saved_limits_layout);
        savedLimitsLayout.removeAllViews(); // Clear previous entries

        Map<String, ?> allEntries = PreferenceHelper.getAllContact();

        boolean isLimitForEveryNumberEnabled = PreferenceHelper.getLimitForAllNumbersEnabled();

        if(action.equals("LoadOnCreate") && (!allEntries.isEmpty() || isLimitForEveryNumberEnabled)){
            // Start foreground service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, CallMonitorService.class));
                Log.d("MainActivity", "Started Foreground Service from OnCreate");
            }
        }
        else if(action.equals("SetLimit") && allEntries.size() == 1){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, CallMonitorService.class));
                Log.d("MainActivity", "Started Foreground Service from SetLimit");
            }
        } else if(action.equals("DeleteTimeLimit") && allEntries.isEmpty() && !isLimitForEveryNumberEnabled){
            Intent stopForegroundService = new Intent(this, CallMonitorService.class);
            stopService(stopForegroundService);
            Log.d("MainActivity", "Stopped Foreground Service from DeleteTimeLimit");
        }

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String phoneNumber = entry.getKey();
            String phoneNumberData = (String) entry.getValue();

            if(phoneNumberData != null){
                try{
                    JSONObject jsonObject = new JSONObject(phoneNumberData);

                    int remaining_time = jsonObject.getInt("remaining_time");
                    String contact_name = "";
                    if(jsonObject.has("name")){
                        contact_name = jsonObject.getString("name");
                    }

                    createLayout(savedLimitsLayout, phoneNumber, remaining_time, contact_name);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }


            }
        }
    }

    private void createLayout(LinearLayout savedLimitsLayout, String phoneNumber, int remaining_time, String contact_name){
        int hours = remaining_time / 3600;
        int minutes = (remaining_time % 3600) / 60;
        int seconds = remaining_time % 60;

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

        // Horizontal Linear Layout to add an edit button
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        EditText identifier = new EditText(this);
        if(!contact_name.isEmpty()){
            identifier.setText(contact_name);
        } else{
            identifier.setText(phoneNumber);
        }
        identifier.setTextSize(20);
        identifier.setTypeface(Typeface.DEFAULT_BOLD);
        identifier.setTag("number");
        identifier.setBackground(null);
        identifier.setFocusable(false);
        identifier.setPadding(0, 0, 0, 0);
        identifier.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.8f));
        identifier.setSingleLine(true);
        identifier.setImeOptions(EditorInfo.IME_ACTION_DONE);
        identifier.setInputType(InputType.TYPE_CLASS_TEXT);

        // Disable Touch Events in Non-Edit Mode to Handle Bottom Sheets
        identifier.setFocusableInTouchMode(false);
        identifier.setEnabled(false);
        identifier.setTextColor(Color.WHITE);
        identifier.setClickable(false);
        identifier.setLongClickable(false);
//        number.setId(View.generateViewId());

        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, R.style.Widget_App_Button_IconOnly);

        MaterialButton iconButton = new MaterialButton(wrapper, null, R.style.Widget_App_Button_IconOnly);
        iconButton.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.2f));
        iconButton.setBackgroundColor(Color.TRANSPARENT);
        iconButton.setIconResource(R.drawable.edit_24px);
        iconButton.setText("");
        iconButton.setStrokeWidth(0);

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        int iconColor = typedValue.data;
        iconButton.setIconTint(ColorStateList.valueOf(iconColor));

        iconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isCurrentlyEditing = iconButton.getTag() != null && (boolean) iconButton.getTag();

                if(!isCurrentlyEditing){
                    iconButton.setTag(true);
                    iconButton.setIconResource(R.drawable.check_24px);

                    enableEditing(identifier);
                } else {
                    iconButton.setTag(false);
                    iconButton.setIconResource(R.drawable.edit_24px);

                    String contactName = Objects.requireNonNull(identifier.getText()).toString().trim();
                    if(contactName.isEmpty()){
                       identifier.setText(phoneNumber);
                    }
                    String phoneNumberData = PreferenceHelper.getContact(phoneNumber);
                    if(phoneNumberData != null){
                        // Saving name
                        try {
                            JSONObject jsonObject = new JSONObject(phoneNumberData);
                            jsonObject.put("name", contactName);
                            PreferenceHelper.saveContact(phoneNumber, jsonObject.toString());

                            updateSavedLimitsUI("Refresh");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    disableEditing(identifier);
                }
            }
        });

        identifier.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_DONE){
                    disableEditing(identifier);
                    return true;
                }
                return false;
            }
        });

        identifier.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                boolean isCurrentlyEditing = iconButton.getTag() != null && (boolean) iconButton.getTag();
                if (!hasFocus && isCurrentlyEditing) {
                    iconButton.performClick();
                }
            }
        });



        header.addView(identifier);
        header.addView(iconButton);

        TextView time = new TextView(this);
        time.setText(hours + " hrs " + minutes + " mins " + seconds + " seconds");
        time.setTextSize(16);

        valueLayout.addView(header);
        valueLayout.addView(time);

        valueLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                boolean isCurrentlyEditing = iconButton.getTag() != null && (boolean) iconButton.getTag();
                if (isCurrentlyEditing) {
                    return true;
                }
                TimerBottomSheet bottomSheet = new TimerBottomSheet(new TimerBottomSheet.OnTimeSelectedListener() {
                    @Override
                    public void onTimeSelected(int hours, int minutes) {
                        System.out.println(hours + " " + minutes);
                        selectedHour = hours;
                        selectedMinute = minutes;

                        String phoneNumberData = PreferenceHelper.getContact(phoneNumber);

                        if(phoneNumberData != null){
                            // Update remaining time
                            try {
                                JSONObject jsonObject = updateLimit(phoneNumberData);
                                PreferenceHelper.saveContact(phoneNumber, jsonObject.toString());

                                updateSavedLimitsUI("Refresh");

                                // Resetting time
                                selectedHour = -1;
                                selectedMinute = -1;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    @NonNull
                    private JSONObject updateLimit(String phoneNumberData) throws JSONException {
                        JSONObject jsonObject = new JSONObject(phoneNumberData);
                        int remainingTime =  jsonObject.getInt("remaining_time");
                        int limit = jsonObject.getInt("limit");
                        int updatedLimit = (selectedHour * 3600) + (selectedMinute * 60);
                        if(limit == remainingTime){
                            jsonObject.put("limit", updatedLimit);
                            jsonObject.put("remaining_time", updatedLimit);
                            return jsonObject;
                        } else if(updatedLimit == remainingTime){
                            jsonObject.put("limit", updatedLimit);
                            jsonObject.put("remaining_time", updatedLimit);
                            return jsonObject;
                        } else if(remaining_time >= updatedLimit){
                            jsonObject.put("limit", updatedLimit);
                            jsonObject.put("remaining_time", updatedLimit);
                            return jsonObject;
                        } else if(limit < updatedLimit){
                            int newTime = updatedLimit - limit + remaining_time;
                            jsonObject.put("limit", updatedLimit);
                            jsonObject.put("remaining_time", newTime);
                            return jsonObject;
                        }
                        return jsonObject;
                    }

                    @Override
                    public void onTimerReset() {
                        isTimeAvailable = false;
                    }
                }, phoneNumber, contact_name);
                bottomSheet.show(getSupportFragmentManager(), "TimerBottomSheet");
                return false;
            }
        });

        // Create a delete button
        MaterialButton deleteButton = new MaterialButton(this);
        deleteButton.setId(View.generateViewId());
        deleteButton.setTextSize(16);
        deleteButton.setCornerRadius(8);
        deleteButton.setIconPadding(0);
        deleteButton.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        deleteButton.setOnClickListener(v -> {
            try {
                deleteTimeLimit(phoneNumber);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.2F));

        Drawable icon = ContextCompat.getDrawable(this, R.drawable.delete_24px);
        deleteButton.setIcon(icon);

        // Add views to layout
        entryLayout.addView(valueLayout);
        entryLayout.addView(deleteButton);
        savedLimitsLayout.addView(entryLayout);
    }

    private void deleteTimeLimit(String phoneNumber) throws JSONException {
        PreferenceHelper.removeContact(phoneNumber);

        // Refresh UI
        updateSavedLimitsUI("DeleteTimeLimit");
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            updateSavedLimitsUI("Refresh");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setupStatusBarAppearance();
    }

    private void setupStatusBarAppearance() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // Let app draw behind system bars
            getWindow().setDecorFitsSystemWindows(false);

            final View rootView = findViewById(android.R.id.content);

            // Handle insets to avoid status bar overlap
            rootView.setOnApplyWindowInsetsListener((v, insets) -> {
                Insets systemBarsInsets = insets.getInsets(WindowInsets.Type.systemBars());
                v.setPadding(0, systemBarsInsets.top, 0, 0); // Top padding only
                return insets;
            });

            // Dynamically adjust status bar text/icons color based on theme
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                boolean isDarkTheme = (getResources().getConfiguration().uiMode
                        & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

                controller.setSystemBarsAppearance(
                        isDarkTheme ? 0 : WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        }
    }

    private void checkAndSetInitialDate() {
        String currentDate = getTodayDate();
        String lastSavedDate = PreferenceHelper.getLastUpdatedDate();

        if (lastSavedDate.isEmpty()) {
            PreferenceHelper.saveLastUpdatedDate(currentDate);
        }
    }

    private String getTodayDate(){
        return new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            android.net.Uri contactUri = data.getData();

            String[] projection = {
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            };

            assert contactUri != null;
            try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                    String phoneNumber = cursor.getString(numberIndex);
                    String name = cursor.getString(nameIndex);

                    String numberWithoutCountryCode;

                    if(phoneNumber.startsWith("+")){
                        numberWithoutCountryCode = String.valueOf(phoneNumberUtil.parse(phoneNumber, null).getNationalNumber());
                    }
                    else {
                        numberWithoutCountryCode = cleanLocalPhoneNumber(phoneNumber);
                    }

                    phoneNumberField.setText(numberWithoutCountryCode);
                    contactNameField.setText(name);
                }
            } catch (Exception e) {
                if(e.toString().contains("Error type: INVALID_COUNTRY_CODE.")){
                    Toast.makeText(this, "Pick valid phone number.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error please try again.", Toast.LENGTH_SHORT).show();
                }
//                throw new RuntimeException(e);
            }
        }
    }

     private String cleanLocalPhoneNumber(String rawNumber) {
        if (rawNumber == null) return "";

        // Remove all characters except digits
        return rawNumber.replaceAll("[^\\d]", "");
    }

    private void setTheme(String selectedTheme){
        switch(selectedTheme){
            case "Light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            break;
            case "Dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    private void enableEditing(EditText editText) {
        editText.setFocusableInTouchMode(true);
        editText.setFocusable(true);
        editText.setEnabled(true);
        editText.setClickable(true);
        editText.setLongClickable(true);
        editText.setSelection(editText.getText().length());
        editText.requestFocus();

        editText.post(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null){
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void disableEditing(EditText editText) {
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.setEnabled(false);
        editText.setTextColor(Color.WHITE);
        editText.setClickable(false);
        editText.setLongClickable(false);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private void clearInputFocus(){
        if(phoneNumberField != null){
            phoneNumberField.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null){
                imm.hideSoftInputFromWindow(phoneNumberField.getWindowToken(), 0);
            }
        }
        if(contactNameField != null){
            contactNameField.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null){
                imm.hideSoftInputFromWindow(contactNameField.getWindowToken(), 0);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            View v = getCurrentFocus();
            if(v instanceof TextInputEditText){
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if(!outRect.contains((int)event.getRawX(), (int)event.getRawY())){
                    clearInputFocus();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
