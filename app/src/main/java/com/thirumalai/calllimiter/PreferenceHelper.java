package com.thirumalai.calllimiter;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class PreferenceHelper {
    private static final String CONTACT_DATA_PREF = "contact_data_store";
    private static final String LAST_UPDATED_PREF = "last_updated_store";
    private static final String LAST_UPDATED_KEY = "last_updated_key";
    private static final String REMAINING_TIME = "remaining_time";
    private static final String LIMIT = "limit";
    private static SharedPreferences contactDataStore, lastUpdatedStore;
    private static SharedPreferences.Editor contactDataEditor, lastUpdatedEditor;

    public static void init(Context context) {
        if (contactDataStore == null) {
            contactDataStore = context.getApplicationContext().getSharedPreferences(CONTACT_DATA_PREF, Context.MODE_PRIVATE);
            contactDataEditor = contactDataStore.edit();
        }
        if(lastUpdatedStore == null){
            lastUpdatedStore = context.getApplicationContext().getSharedPreferences(LAST_UPDATED_PREF, Context.MODE_PRIVATE);
            lastUpdatedEditor = lastUpdatedStore.edit();
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

//    public static Map<String, ?> setAllContactLimits(Context context) {
//        Map<String, ?> all = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getAll();
//        Map<String, String> result = new HashMap<>();
//        for (String key : all.keySet()) {
//            result.put(key, (String) all.get(key));
//        }
//        return result;
//    }
}
