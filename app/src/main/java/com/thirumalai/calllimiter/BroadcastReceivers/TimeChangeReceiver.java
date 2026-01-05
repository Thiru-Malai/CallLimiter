package com.thirumalai.calllimiter.BroadcastReceivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.thirumalai.calllimiter.PreferenceHelper;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class TimeChangeReceiver extends BroadcastReceiver {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override

    public void onReceive(Context context, Intent intent) {
        PreferenceHelper.init(context);
        resetTime(context);
        Log.d("TimeChangeReceiver", "Time Reset");
    }

    public void resetTime(Context context){
        updateDate(context);

        Map<String, ?> all = PreferenceHelper.getAllContact();
        for (String phoneNumber : all.keySet()) {
            try{
                JSONObject jsonObject = new JSONObject((String) Objects.requireNonNull(all.get(phoneNumber)));

                int limit = jsonObject.getInt("limit");
                jsonObject.put("remaining_time", limit);

                PreferenceHelper.saveContact(phoneNumber, jsonObject.toString());
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void updateDate(Context context) {
        String currentDate = getTodayDate();
        String last_updated = PreferenceHelper.getLastUpdatedDate();

        if (!last_updated.isEmpty() && !last_updated.equals(currentDate)) {
                PreferenceHelper.saveLastUpdatedDate(currentDate);;
        }
    }

    private String getTodayDate(){
        return new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
    }
}
