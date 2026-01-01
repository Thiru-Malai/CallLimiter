package com.thirumalai.calllimiter.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.thirumalai.calllimiter.service.CallMonitorService;

public class CancelTimerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        CallMonitorService service = CallMonitorService.getInstance();
        if(service != null){
            service.stopCallTimer();
        }
    }
}
