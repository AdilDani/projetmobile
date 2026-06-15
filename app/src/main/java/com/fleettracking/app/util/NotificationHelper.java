package com.fleettracking.app.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fleettracking.app.R;
import com.fleettracking.app.admin.EntretiensActivity;

public class NotificationHelper {

    private static final String CHANNEL_MAINTENANCE = "fleet_maintenance";
    private static int maintenanceNotifId = 2000;

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

    public static void ensureMaintenanceChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_MAINTENANCE,
                    ctx.getString(R.string.notif_channel_maintenance_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(ctx.getString(R.string.notif_channel_maintenance_desc));
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

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

    public static void notifyMaintenance(Context ctx, String text, String vehiculeId) {
        if (!new Prefs(ctx).isNotificationsEnabled()) return;
        ensureMaintenanceChannel(ctx);

        Intent intent = new Intent(ctx, EntretiensActivity.class);
        if (vehiculeId != null) intent.putExtra(EntretiensActivity.EXTRA_VEHICULE_ID, vehiculeId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(ctx, maintenanceNotifId,
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(ctx, CHANNEL_MAINTENANCE)
                .setSmallIcon(R.drawable.ic_wrench)
                .setContentTitle(ctx.getString(R.string.notif_maintenance_title))
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        try {
            NotificationManagerCompat.from(ctx).notify(maintenanceNotifId++, notification);
        } catch (SecurityException ignored) {}
    }
}
