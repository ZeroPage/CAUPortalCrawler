package test.apple.lemon.cauportalcrawlertest.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.HashMap;
import java.util.List;

import test.apple.lemon.cauportalcrawlertest.AppDelegate;
import test.apple.lemon.cauportalcrawlertest.R;
import test.apple.lemon.cauportalcrawlertest.activity.mainfront.TabActivity;
import test.apple.lemon.cauportalcrawlertest.model.EClassContent;


public class NotificationIntentService extends IntentService {

    private static final int NOTIFICATION_ID = 1;

    public NotificationIntentService() {
        super("NotificationIntentService");
    }

    public static Intent getStartIntent(Context context) {
        return new Intent(context, NotificationIntentService.class);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        assert intent != null;
        RuntimeExceptionDao<EClassContent, Integer> dao = AppDelegate.getHelper(getApplicationContext()).getContentsDAO();
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(EClassContent.ALREADY_READ_FIELD, false);
        List<EClassContent> eClassContents = dao.queryForFieldValues(map);
        int size = eClassContents.size();

        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, TabActivity.class), 0);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText(String.format("안 읽은 CAU E-Class 알림이 %,d개 있습니다.", size));
        Notification notification = mBuilder.build();
        notification.flags |= Notification.DEFAULT_SOUND;

        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }


}
