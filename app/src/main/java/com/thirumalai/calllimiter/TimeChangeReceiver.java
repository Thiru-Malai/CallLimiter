package com.thirumalai.calllimiter;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class TimeChangeReceiver extends BroadcastReceiver {

    private final String SHARED_PREF_NAME = "limit_prefs";
    private static final String LAST_UPDATED_DATE = "last_updated_key";
    private static SharedPreferences sharedPreferences;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override

    public void onReceive(Context context, Intent intent) {
        resetTime(context);
        Log.d("TimeChangeReceiver", "Time Reset");
    }

    public void resetTime(Context context){
        updateDate(context);

        Map<String, ?> all = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).getAll();
        for (String phoneNumber : all.keySet()) {
            try{
                JSONObject jsonObject = new JSONObject((String) Objects.requireNonNull(all.get(phoneNumber)));

                int limit = jsonObject.getInt("limit");
                jsonObject.put("remaining_time", limit);

                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString(phoneNumber, jsonObject.toString());
                editor.apply();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
//        return result;
    }

    private void updateDate(Context context) {
        String LAST_UPDATED_PREF = "last_updated_pref";
        String currentDate = getTodayDate();

        sharedPreferences = context.getSharedPreferences(LAST_UPDATED_PREF, MODE_PRIVATE);
        String last_updated = sharedPreferences.getString(LAST_UPDATED_DATE, "");

        if (last_updated.isEmpty() && !last_updated.equals(currentDate)) {
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString(LAST_UPDATED_DATE, currentDate);
                editor.apply();
        }
    }

    private String getTodayDate(){
        return new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
    }
}
