package com.fleettracking.app.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.fleettracking.app.R;

/**
 * Builds and posts local system notifications. Creates the notification
 * channel on Android O+ and respects the user's notification preference.
 */
public class NotificationHelper {

    private NotificationHelper() {}

    public static void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    ctx.getString(R.string.notif_channel_id),
                    ctx.getString(R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(ctx.getString(R.string.notif_channel_desc));
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    /** Posts a notification, honoring the user's in-app notification toggle. */
    public static void notifyIncidentSent(Context ctx) {
        if (!new Prefs(ctx).isNotificationsEnabled()) return;
        ensureChannel(ctx);

        Notification notification = new NotificationCompat.Builder(
                ctx, ctx.getString(R.string.notif_channel_id))
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(ctx.getString(R.string.notif_incident_title))
                .setContentText(ctx.getString(R.string.notif_incident_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        NotificationManager nm =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify((int) (System.currentTimeMillis() & 0xfffffff), notification);
    }
}
