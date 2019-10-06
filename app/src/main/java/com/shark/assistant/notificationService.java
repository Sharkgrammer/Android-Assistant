package com.shark.assistant;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.util.Log;

public class notificationService extends NotificationListenerService {

    Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public void onDestroy (){
        onNotificationPosted(null);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            String pack = sbn.getPackageName();
            Bundle extras = sbn.getNotification().extras;
            String title = extras.getString("android.title");

            Object textObj = extras.get("android.text");

            String text = null;
            try{
                if (textObj instanceof String){
                    text = (String) textObj;
                }else{
                    SpannableString textString = (SpannableString) textObj;

                    CharSequence charSeq = textString.subSequence(0, textString.length());

                    text = charSeq.toString();
                }


            }catch(Exception e){
                Log.wtf("textObj Error", e.toString());
            }

            if (pack == null) pack = "";
            if (title == null) title = "";
            if (text == null) text = "";

            Intent msg = new Intent("Msg");
            msg.putExtra("package", pack);
            msg.putExtra("title", title);
            msg.putExtra("text", text);

            LocalBroadcastManager.getInstance(context).sendBroadcast(msg);
        } catch (Exception e) {
            Log.wtf("Error in onNotificationPosted", e.toString());
        }
    }

}
