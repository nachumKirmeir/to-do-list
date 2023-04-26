package com.nachumToDoApp.vr2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = context.getSharedPreferences("timer", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("alarm", "false");
        String position = sp.getString("position", "0");
        String task = sp.getString("taskMessage", "");
        editor.apply();
        // Show notification for the timer at the given position
        showNotification(context, Integer.valueOf(position) + 1, task);
    }

    private void showNotification(Context context, int position, String task) {
        int icon = (int)R.drawable.ic_baseline_timer_24;
        String title = "Timer finished";
        long when = System.currentTimeMillis();
        String ticker = "ticker";
        String text= "Task #" + position + " has finished its timer";
        if(task != null) text = "Task: " + task + "  #" + position + " has finished its timer";

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "M_CH_ID");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "YOUR_CHANNEL_ID";
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }


        Notification notification = builder.setContentIntent(pendingIntent).setSmallIcon(icon).setTicker(ticker).setWhen(when)
                .setAutoCancel(true).setContentTitle(title)
                .setContentText(text).build();

        notificationManager.notify(position, notification);
    }
}