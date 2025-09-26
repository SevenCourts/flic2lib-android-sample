package flic.io.flic2androidsample;

import android.app.ForegroundServiceStartNotAllowedException;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class Flic2SampleService extends Service {
    public static final String START_ACTION = "START";
    public static final String STOP_ACTION = "STOP";

    private static final int SERVICE_NOTIFICATION_ID = 123;
    private final String NOTIFICATION_CHANNEL_ID = "Notification_Channel_Flic2SampleService";
    private final CharSequence NOTIFICATION_CHANNEL_NAME = "Flic2Sample";

    private boolean runningInForegroundForSure = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            // Restart of the service after e.g. process crash or permission revocation
            return START_STICKY;
        }

        // The stop action is not used right now but might be used in case you want to stop the service,
        // e.g. in case there are no buttons paired or similar.
        if (STOP_ACTION.equals(intent.getAction())) {
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        if (runningInForegroundForSure) {
            return START_STICKY;
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(mChannel);
        }

        Notification notification = new NotificationCompat.Builder(this.getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Flic2Sample")
                .setContentText("Flic2Sample")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                startForeground(SERVICE_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
            } catch (ForegroundServiceStartNotAllowedException | SecurityException e) {
                stopSelf(startId);
                return START_NOT_STICKY;
            }
        } else {
            startForeground(SERVICE_NOTIFICATION_ID, notification);
        }
        runningInForegroundForSure = true;
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public static void createAndStart(Context context) {
        context = context.getApplicationContext();
        try {
            ContextCompat.startForegroundService(context, new Intent(context, Flic2SampleService.class).setAction(START_ACTION));
        } catch (Exception e) {
            if (e instanceof SecurityException || (Build.VERSION.SDK_INT >= 31 && e instanceof ForegroundServiceStartNotAllowedException)) {
            } else {
                throw e;
            }
        }
    }

    public static class BootUpReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // The Application class's onCreate has already been called at this point
            createAndStart(context);
        }
    }

    public static class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // The Application class's onCreate has already been called at this point
            createAndStart(context);
        }
    }
}
