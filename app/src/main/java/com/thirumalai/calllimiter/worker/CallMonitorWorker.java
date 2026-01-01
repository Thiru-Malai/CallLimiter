package com.thirumalai.calllimiter.worker;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.thirumalai.calllimiter.service.CallMonitorService;

public class CallMonitorWorker extends Worker {
    public CallMonitorWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        Intent serviceIntent = new Intent(context, CallMonitorService.class);
        ContextCompat.startForegroundService(context, serviceIntent);
        Log.d("BootReceiver", "Started Foreground Service from Boot Receiver");
        return Result.success();
    }
}
