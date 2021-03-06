package lemon.apple.caution;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.danlew.android.joda.JodaTimeAndroid;

import lemon.apple.caution.model.helper.OrmLiteHelper;
import timber.log.Timber;

/**
 * Created by rino0601 on 2014. 11. 12..
 */
public class AppDelegate extends Application {

    private static volatile OrmLiteHelper databaseHelper = null;

    public static synchronized OrmLiteHelper getHelper(Context context) {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(context.getApplicationContext(), OrmLiteHelper.class);
        }
        return databaseHelper;
    }

    public static synchronized void closeHelper(Context context) {
        OpenHelperManager.releaseHelper();
        getHelper(context).close();
        databaseHelper = null;
        context.deleteDatabase(OrmLiteHelper.DATABASE_NAME);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Crashlytics.start(this);
            Timber.plant(new CrashlyticsTree());
        }
        JodaTimeAndroid.init(this);
    }
}
