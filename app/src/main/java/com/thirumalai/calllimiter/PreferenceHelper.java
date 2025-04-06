package com.thirumalai.calllimiter;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class PreferenceHelper {
    private static final String LAST_UPDATED_PREF = "last_updated_pref";
    private static final String LAST_UPDATED_DATE = "last_updated_key";
    private static final String REMAINING_TIME = "remaining_time";
    private static final String LIMIT = "limit";


    public static void saveLastUpdatedDate(MainActivity context, String date) {
        context.getSharedPreferences(LAST_UPDATED_PREF, Context.MODE_PRIVATE)
                .edit()
                .putString(LAST_UPDATED_DATE, date)
                .apply();
    }

    public static String getLastUpdatedDate(MainActivity context) {
        return context.getSharedPreferences(LAST_UPDATED_PREF, Context.MODE_PRIVATE)
                .getString(LAST_UPDATED_DATE, "");
    }

//    public static void saveContactLimit(Context context, String number, int limitInSeconds) {
//        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//                .edit()
//                .putInt(LIMIT_PREFIX + number, limitInSeconds)
//                .apply();
//    }
//
//    public static int getContactLimit(Context context, String number) {
//        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//                .getInt(LIMIT_PREFIX + number, 0);
//    }

//    public static Map<String, ?> getAllContactLimits(Context context) {
//        Map<String, ?> all = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getAll();
//        Map<String, String> result = new HashMap<>();
//        for (String key : all.keySet()) {
//            result.put(key, (String) all.get(key));
//        }
//        return result;
//    }
//
//    public static Map<String, ?> setAllContactLimits(Context context) {
//        Map<String, ?> all = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getAll();
//        Map<String, String> result = new HashMap<>();
//        for (String key : all.keySet()) {
//            result.put(key, (String) all.get(key));
//        }
//        return result;
//    }
}
