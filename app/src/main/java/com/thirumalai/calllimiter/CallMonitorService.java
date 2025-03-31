package com.thirumalai.calllimiter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class CallMonitorService extends Service {
    private static final String CHANNEL_ID = "CallMonitorChannel";
    private TelephonyManager telephonyManager;
    private Handler handler = new Handler();
    private Runnable endCallRunnable;
    private int CALL_TIME_LIMIT = 5 * 1000; // 30 seconds
    private PhoneStateListener phoneStateListener;
    private PhoneNumberUtil phoneNumberUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        // TODO: For future release
//        createNotificationChannel();
        startForeground(1, getNotification());

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        phoneNumberUtil = PhoneNumberUtil.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                super.onCallStateChanged(state, phoneNumber);
                Log.d("CallMonitorService", phoneNumber);
                Phonenumber.PhoneNumber number;
                try {
                    number = phoneNumberUtil.parse(phoneNumber, null);
//                    number.clearCountryCode();
                    String numberWithoutCountryCode = String.valueOf(number.getNationalNumber());
                    System.out.println(numberWithoutCountryCode);
                    if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                        SharedPreferences sharedPreferences = getSharedPreferences("time_limits", MODE_PRIVATE);
                        int totalSeconds = sharedPreferences.getInt(numberWithoutCountryCode, -1);

                        if(totalSeconds != -1){
                            // Adding +10 secs - timer starts once the call is made and not when the call is attended
                            CALL_TIME_LIMIT = totalSeconds * 1000 + 10000;
                            startCallTimer();
                            Log.d("CallMonitorService", "Call started. Starting timer.");
                        } else {
                            Log.d("callService", "number not present");
                        }
                    } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                        Log.d("CallMonitorService", "Call ended. Stopping timer.");
                        stopCallTimer();
                    }
                } catch (NumberParseException e) {
                    Log.e("CallMonitoringService", "error during parsing a number");
                }
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        return START_STICKY; // Ensures the service restarts if killed
    }

    private void startCallTimer() {
        endCallRunnable = new Runnable() {
            @Override
            public void run() {
                endCall();
            }
        };
        handler.postDelayed(endCallRunnable, CALL_TIME_LIMIT);
    }

    private void stopCallTimer() {
        if (endCallRunnable != null) {
            handler.removeCallbacks(endCallRunnable);
        }
    }

    private void endCall() {
        try {
            TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                boolean success = telecomManager.endCall();
                Log.d("CallMonitorService", "Call ended: " + success);
            } else {
                Log.e("CallMonitorService", "TelecomManager not available or Android version too low.");
            }
        } catch (Exception e) {
            Log.e("CallMonitorService", "Error ending call: " + e.getMessage());
        }
    }
// TODO: For future release

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Call Monitor Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification getNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Call Monitor Running")
                .setContentText("Tracking call duration")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (telephonyManager != null && phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }
}
