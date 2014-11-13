package lemon.apple.caution.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import lemon.apple.caution.AppDelegate;
import lemon.apple.caution.R;
import lemon.apple.caution.activity.caufsm.CAUFSMActivity;
import lemon.apple.caution.activity.mainfront.TabActivity;
import lemon.apple.caution.model.EClassContent;

public class CAUIntentService extends IntentService {

    public CAUIntentService() {
        super("NotificationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        INTENT.onHandleIntent(this, intent);
    }

    public static class INTENT {
        public static void onHandleIntent(CAUIntentService intentService, Intent intent) {
            for (Class<?> clazz : INTENT.class.getDeclaredClasses()) {
                if (clazz.getSimpleName().equals(intent.getAction())) {
                    try {
                        IntentServiceInterface instance = (IntentServiceInterface) clazz.newInstance();
                        instance.onHandleIntent(intentService, intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            // unexpected INTENT arrived.
        }

        private static interface IntentServiceInterface {
            void onHandleIntent(CAUIntentService intentService, Intent intent);
        }

        public static class NOTIFY implements IntentServiceInterface {

            private static final int NOTIFICATION_ID = 1;

            public static void start(Context context) {
                Intent intent = new Intent();
                intent.setAction(NOTIFY.class.getSimpleName());
                intent.setClass(context, CAUIntentService.class);
                context.startService(intent);
            }

            @Override
            public void onHandleIntent(CAUIntentService intentService, Intent intent) {
                assert intent != null;
                RuntimeExceptionDao<EClassContent, Integer> dao = AppDelegate.getHelper(intentService).getContentsDAO();
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put(EClassContent.ALREADY_READ_FIELD, false);
                List<EClassContent> eClassContents = dao.queryForFieldValues(map);
                int size = eClassContents.size();

                if (size != 0) {
                    NotificationManager mNotificationManager = (NotificationManager)
                            intentService.getSystemService(Context.NOTIFICATION_SERVICE);
                    PendingIntent contentIntent = PendingIntent.getActivity(intentService, 0,
                            new Intent(intentService, TabActivity.class), 0);
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(intentService)
                                    .setAutoCancel(true)
                                    .setContentIntent(contentIntent)
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setContentTitle(intentService.getResources().getString(R.string.app_name))
                                    .setContentText(String.format("안 읽은 CAU E-Class 알림이 %,d개 있습니다.", size));
                    Notification notification = mBuilder.build();
                    notification.flags |= Notification.DEFAULT_SOUND;

                    mNotificationManager.notify(NOTIFICATION_ID, notification);
                }

            }
        }

        public static class REGISTER_ALARM implements IntentServiceInterface {

            private static PendingIntent getPendingIntent(Context context) {
                Intent intent = new Intent();
                intent.setAction(REGISTER_ALARM.class.getSimpleName());
                intent.setClass(context, CAUIntentService.class);
                return PendingIntent.getService(context, 0, intent, 0);
            }

            public static void setAlarm(Context context, Date time) {
                PendingIntent pendingIntent = getPendingIntent(context);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, time.getTime(), pendingIntent);
                //alarmManager.setRepeating();
            }

            public static void cancelAlarm(Context context) {
                PendingIntent pendingIntent = getPendingIntent(context);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
            }

            @Override
            public void onHandleIntent(final CAUIntentService intentService, Intent intent) {
                // when alarm occur.

                // check network state
                ConnectivityManager connectivityManager = (ConnectivityManager) intentService.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) { // Check network connection
                    {// just for demo.
                        PowerManager pm = (PowerManager) intentService.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                                        | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                                "wakeup");
                        wl.acquire();
                        // do work.
                        //wl.release();
                    }
                    CAUFSMActivity.start(intentService);

                } else {
                    Handler handler = new Handler();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(intentService, "Wi-Fi가 켜져 있지 않아, E-Class에 접속하지 않았습니다.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }

        public static class INSTANT_START_FSM implements IntentServiceInterface {
            public static void start(Context context) {
                Intent intent = new Intent();
                intent.setAction(INSTANT_START_FSM.class.getSimpleName());
                intent.setClass(context, CAUIntentService.class);
                context.startService(intent); // 일단 IntentService에게 중계.
            }

            @Override
            public void onHandleIntent(CAUIntentService intentService, Intent intent) {
                CAUFSMActivity.start(intentService);
            }
        }
    }
}
