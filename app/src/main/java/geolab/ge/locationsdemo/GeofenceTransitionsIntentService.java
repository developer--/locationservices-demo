package geolab.ge.locationsdemo;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * Created by kot on 1/16/18.
 */

public class GeofenceTransitionsIntentService extends IntentService{
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public GeofenceTransitionsIntentService() {
        super("geofenceing service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        assert geofencingEvent != null;
        if (geofencingEvent.hasError()){
            int errCode = geofencingEvent.getErrorCode();
            Log.e("INTENT_SERVICE_LOG", "Error code " + errCode);
            return;
        }

        shoNotification("event triggered",1);

        int geofenceTransitionType = geofencingEvent.getGeofenceTransition();
        if (geofenceTransitionType == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransitionType == Geofence.GEOFENCE_TRANSITION_EXIT){
            List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();
            if (!triggeredGeofences.isEmpty()) {
                shoNotification(triggeredGeofences.get(0).getRequestId(), 0);
            }
        }
    }

    private void shoNotification(String title, int i){
        String CHANNEL_ID = "my_channel_01";
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("geofence")
                        .setContentText(title);
        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(i, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        assert mNotificationManager != null;
        mNotificationManager.notify(i, mBuilder.build());
    }
}
