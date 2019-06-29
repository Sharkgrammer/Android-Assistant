package com.shark.assistant;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;

public class NotificationService extends NotificationListenerService {

    Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pack = sbn.getPackageName();
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString("android.title");
        String text = extras.getString("android.text");

        if (pack == null) pack = "";
        if (title == null) title = "";
        if (text == null) text = "";

        Intent msg = new Intent("Msg");
        msg.putExtra("package", pack);
        msg.putExtra("title", title);
        msg.putExtra("text", text);

        LocalBroadcastManager.getInstance(context).sendBroadcast(msg);
    }

}
