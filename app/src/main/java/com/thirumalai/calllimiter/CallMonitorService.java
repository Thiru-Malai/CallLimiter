package com.thirumalai.calllimiter;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
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
import com.thirumalai.calllimiter.BroadcastReceivers.CancelTimerReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CallMonitorService extends Service {
    private static final String CHANNEL_ID = "CallMonitorChannel";
    private TelephonyManager telephonyManager;
    private Handler handler = new Handler();
    private Runnable endCallRunnable;
    private int callTimeLimit = 10 * 1000; // 10 seconds
    private PhoneStateListener phoneStateListener;
    private PhoneNumberUtil phoneNumberUtil;
    private boolean isTimerRunning = false;
    private int elapsedTime = 1; // Time in seconds
    private static CallMonitorService instance;
    private PendingIntent pendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        PreferenceHelper.init(this);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneNumberUtil = PhoneNumberUtil.getInstance();

        Intent clickIntent = new Intent(this, CancelTimerReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, getNotification(), FOREGROUND_SERVICE_TYPE_PHONE_CALL);
        }

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                super.onCallStateChanged(state, phoneNumber);

                Log.d("CallMonitorService", phoneNumber);
                Phonenumber.PhoneNumber number;
                try {
                    String numberWithoutCountryCode = phoneNumber;
                    if(phoneNumber.startsWith("+")){
                        number = phoneNumberUtil.parse(phoneNumber, null);
                        numberWithoutCountryCode = String.valueOf(number.getNationalNumber());
                        System.out.println(numberWithoutCountryCode);
                    }
                    if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                        elapsedTime = 1;

                        String phoneNumberData = PreferenceHelper.getContact(numberWithoutCountryCode);

                        boolean isTimeLimitForAllNumbersEnabled = PreferenceHelper.getLimitForAllNumbersEnabled();
                        if(phoneNumberData == null && isTimeLimitForAllNumbersEnabled){
                            JSONObject newNumber = new JSONObject();
                            newNumber.put("remaining_time", PreferenceHelper.getTimeLimitForAllNumbers());
                            newNumber.put("limit", PreferenceHelper.getTimeLimitForAllNumbers());
                            newNumber.put("last_updated", getTodayDate());

                            PreferenceHelper.saveContact(numberWithoutCountryCode, newNumber.toString());
                            phoneNumberData = newNumber.toString();
                            Log.d("CallMonitorService", "Adding new number since limit for all numbers is enabled");
                        }

                        if(phoneNumberData != null){
                            JSONObject jsonObject = new JSONObject(phoneNumberData);
                            int remaining_time = jsonObject.getInt("remaining_time");
                            boolean isBufferEnabled = PreferenceHelper.isBufferEnabled();
                            int bufferTime = PreferenceHelper.getBufferTime();
                            if(isBufferEnabled && remaining_time < 10 ){
                                remaining_time = bufferTime;
                            }

                            // Adding +10 secs - timer starts once the call is made and not when the call is attended
                            callTimeLimit = remaining_time * 1000 + 10000;
                            System.out.println(callTimeLimit);
                            startCallTimer();

                            Log.d("CallMonitorService", "Call started. Starting timer.");
                        } else {

                            Log.d("callService", "number not present");
                        }
                    } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                        Log.d("CallMonitorService", "Call State Idle Triggered");

                        String phoneNumberData = PreferenceHelper.getContact(numberWithoutCountryCode);
                        if(phoneNumberData != null){
                            Log.d("CallMonitorService", "Call ended. Stopping timer.");

                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            if(vibrator != null && vibrator.hasVibrator()){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    VibrationEffect vibrationEffect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE);
                                    vibrator.vibrate(vibrationEffect);
                                }
                                else{
                                    vibrator.vibrate(200);
                                }
                            }

                            // Update remaining time

                            try {
                                JSONObject jsonObject = new JSONObject(phoneNumberData);
                                int remainingTime =  callTimeLimit / 1000 - elapsedTime;

                                if(remainingTime < 0){
                                    remainingTime = 0;
                                }

                                jsonObject.put("remaining_time", remainingTime);

                                PreferenceHelper.saveContact(numberWithoutCountryCode, jsonObject.toString());

                                if(isTimerRunning){
                                    stopCallTimer();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                } catch (NumberParseException e) {
                    Log.e("CallMonitoringService", "error during parsing a number");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
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
        handler.postDelayed(endCallRunnable, callTimeLimit);
        handler.post(updateRunnable);
    }

    public void stopCallTimer() {
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
                boolean isTimerCancelDisabled = PreferenceHelper.getDisableTimerCancelStatus();
                if(!isTimerCancelDisabled) {
                    updateTimerNotification();
                }
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
                .setContentTitle("Tap here to stop call timer")
                .setContentText("Time Left: " + formatTime((callTimeLimit / 1000) - elapsedTime))
                .setSmallIcon(R.drawable.logo___notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_v2))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
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

    public static CallMonitorService getInstance() {
        return instance;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;

        if (telephonyManager != null && phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private String getTodayDate(){
        return new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
    }
}
