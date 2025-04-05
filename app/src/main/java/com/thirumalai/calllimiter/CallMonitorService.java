package com.thirumalai.calllimiter;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
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
    private int CALL_TIME_LIMIT = 10 * 1000; // 10 seconds
    private PhoneStateListener phoneStateListener;
    private PhoneNumberUtil phoneNumberUtil;
    private boolean isTimerRunning = false;
    private int elapsedTime = 1; // Time in seconds
    private SharedPreferences sharedPreferences;
    private final String SHARED_PREF_NAME = "time_limits";

    @Override
    public void onCreate() {
        super.onCreate();

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        phoneNumberUtil = PhoneNumberUtil.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground(1, getNotification());

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                super.onCallStateChanged(state, phoneNumber);

                Log.d("CallMonitorService", phoneNumber);
                Phonenumber.PhoneNumber number;
                try {
                    number = phoneNumberUtil.parse(phoneNumber, null);
                    String numberWithoutCountryCode = String.valueOf(number.getNationalNumber());
                    System.out.println(numberWithoutCountryCode);
                    if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                        elapsedTime = 1;
                        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
                        int totalSeconds = sharedPreferences.getInt(numberWithoutCountryCode, -1);

                        if(totalSeconds == 0){
                            totalSeconds = 10;
                        }

                        if(totalSeconds != -1){
                            // Adding +10 secs - timer starts once the call is made and not when the call is attended
                            CALL_TIME_LIMIT = totalSeconds * 1000 + 10000;
                            System.out.println(CALL_TIME_LIMIT);
                            startCallTimer();

                            Log.d("CallMonitorService", "Call started. Starting timer.");
                        } else {
                            Log.d("callService", "number not present");
                        }
                    } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                        Log.d("CallMonitorService", "Call ended. Stopping timer.");

                        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
                        int totalSeconds = sharedPreferences.getInt(numberWithoutCountryCode, -1);

                        if(totalSeconds != -1){
                            // Update remaining time
                            sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            // Save the phone number as the key and time limit as the value
                            System.out.println("TIME UPDATED " + (CALL_TIME_LIMIT/1000 - elapsedTime));
                            editor.putInt(numberWithoutCountryCode, CALL_TIME_LIMIT / 1000 - elapsedTime);
                            editor.apply();

                            stopCallTimer();
                        }
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
        if (!isTimerRunning) {
            isTimerRunning = true;
        }
        endCallRunnable = () -> {
            endCall();
        };
        handler.postDelayed(endCallRunnable, CALL_TIME_LIMIT);
        handler.post(updateRunnable);
    }

    private void stopCallTimer() {
        if (isTimerRunning) {
            isTimerRunning = false;
        }
        if (endCallRunnable != null) {
            handler.removeCallbacks(endCallRunnable);
            handler.removeCallbacks(updateRunnable); // Stop updating notification

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(1, getNotification()); // Revert to "Limiting Call Duration" notification
            }
        }
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTimerRunning) {
                elapsedTime++;
                updateTimerNotification();
                handler.postDelayed(this, 1000); // Repeat every second
            }
        }
    };

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
                .setContentTitle("Call Monitor Active")
                .setContentText("Limiting call duration")
                .setSmallIcon(R.drawable.logo___notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_v2))
                .build();
    }

    private void updateTimerNotification() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Call Monitor Active")
                .setContentText("Time Left: " + formatTime((CALL_TIME_LIMIT / 1000) - elapsedTime))
                .setSmallIcon(R.drawable.logo___notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_v2))
                .setOngoing(true)
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (manager != null) {
            manager.notify(1, notification); // Update the existing notification
        }
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(int seconds) {
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;

        if (h > 0) {
            return String.format("%02d:%02d:%02d", h, m, s); // hh:mm:ss
        } else {
            return String.format("%02d:%02d", m, s); // mm:ss
        }
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
