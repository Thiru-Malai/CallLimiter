package com.thirumalai.calllimiter.BroadcastReceivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.thirumalai.calllimiter.CallMonitorService;
import com.thirumalai.calllimiter.PreferenceHelper;

public class BootReceiver extends BroadcastReceiver {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BootReceiver", "Starting Foreground");
        PreferenceHelper.init(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int activeLimitContactSize = PreferenceHelper.getAllContactSize();
            if(activeLimitContactSize > 0){
//                OneTimeWorkRequest workRequest =
//                        new OneTimeWorkRequest.Builder(CallMonitorWorker.class)
//                                .setInitialDelay(10, TimeUnit.SECONDS) // delay avoids crash
//                                .build();
//                WorkManager.getInstance(context).enqueue(workRequest);
                context.startService(new Intent(context, CallMonitorService.class));
                Log.d("BootReceiver", "Started Foreground Service from Boot Receiver");
            }
        }
    }
}
