package parking.group6.csc413.projectmap;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

/**
 * <h1>AlarmReceiver is a helper class that receives the BroadCast. It extend android.content.BroadcastReceiver class</h1>
 * <p> The class overrides onReceive method to vake up the lock and push the notification using the NotificationCompat builder</p>
 * <b> Authors are defined as anyone who wrote code for the class.</b>
 * @author csc 413 group 6
 * @version 1
 * @see android.content.BroadcastReceiver
 */
public class AlarmReceiver extends BroadcastReceiver {
    int mid;

    /**
     * This method receives the broadcast intent and fires the vibration and push notification.
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Log.e("onReceive", "Here ");
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(( PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
        intent.putExtra("mess","TIME IS DOME" );
        if ((wakeLock != null) &&           // we have a WakeLock
                (wakeLock.isHeld() == false)) {  // but we don't hold it
            wakeLock.acquire();
        }
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(2000);


        Intent resultIntent = new Intent(context, MapsActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.putExtra("mess", "Time up");
        context.startActivity(resultIntent);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("SanFran ParkIt")
                        .setContentText("Time is up !");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MapsActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mid, mBuilder.build());

    }
}