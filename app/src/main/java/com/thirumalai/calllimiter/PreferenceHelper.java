package com.thirumalai.calllimiter;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class PreferenceHelper {
    private static final String CONTACT_DATA_PREF = "contact_data_store";
    private static final String LAST_UPDATED_PREF = "last_updated_store";
    private static final String FIRST_TIME_PREF = "first_time_store";
    private static final String LAST_UPDATED_KEY = "last_updated_key";
    private static final String FIRST_TIME_KEY = "first_time_key";
    private static final String REMAINING_TIME = "remaining_time";
    private static final String LIMIT = "limit";
    private static final String SETTINGS_PREF = "settings_store";
    private static final String THEME_KEY = "theme_key";
    private static SharedPreferences contactDataStore, lastUpdatedStore, firstTimeStore, settingsStore;
    private static SharedPreferences.Editor contactDataEditor, lastUpdatedEditor, firstTimeEditor, settingsEditor;

    public static void init(Context context) {
        if (contactDataStore == null) {
            contactDataStore = context.getApplicationContext().getSharedPreferences(CONTACT_DATA_PREF, Context.MODE_PRIVATE);
            contactDataEditor = contactDataStore.edit();
        }
        if(lastUpdatedStore == null){
            lastUpdatedStore = context.getApplicationContext().getSharedPreferences(LAST_UPDATED_PREF, Context.MODE_PRIVATE);
            lastUpdatedEditor = lastUpdatedStore.edit();
        }
        if(firstTimeStore == null){
            firstTimeStore = context.getApplicationContext().getSharedPreferences(FIRST_TIME_PREF, Context.MODE_PRIVATE);
            firstTimeEditor = firstTimeStore.edit();
        }
        if(settingsStore == null){
            settingsStore = context.getApplicationContext().getSharedPreferences(SETTINGS_PREF, Context.MODE_PRIVATE);
            settingsEditor = settingsStore.edit();
        }
    }

    public static void saveLastUpdatedDate(String date) {
        lastUpdatedEditor.putString(LAST_UPDATED_KEY, date).apply();
    }

    public static String getLastUpdatedDate() {
        return lastUpdatedStore.getString(LAST_UPDATED_KEY, "");
    }

    public static void saveContact(String phoneNumber, String data) {
        contactDataEditor.putString(phoneNumber, data).apply();
    }

    public static void removeContact(String phoneNumber) {
        contactDataEditor.remove(phoneNumber).apply();
    }

    public static String getContact(String number) {
        return contactDataStore.getString(number, null);
    }

    public static Map<String, ?> getAllContact() {
        return contactDataStore.getAll();
    }

    public static int getAllContactSize(){
        return  contactDataStore.getAll().size();
    }

    public static boolean isFirstTimeLogin(){
        return firstTimeStore.getBoolean(FIRST_TIME_KEY, true);
    }

    public static void setOnboardingCompleted(){
        firstTimeEditor.putBoolean(FIRST_TIME_KEY, false).apply();
    }

    public static void saveTheme(String theme){
        settingsEditor.putString(THEME_KEY, theme).apply();
    }

    public static String getTheme(){
        return settingsStore.getString(THEME_KEY, "System");
    }

//    public static Map<String, ?> setAllContactLimits(Context context) {
//        Map<String, ?> all = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getAll();
//        Map<String, String> result = new HashMap<>();
//        for (String key : all.keySet()) {
//            result.put(key, (String) all.get(key));
//        }
//        return result;
//    }
}
