package test.apple.lemon.cauportalcrawlertest;

import android.app.Application;
import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.danlew.android.joda.JodaTimeAndroid;

import test.apple.lemon.cauportalcrawlertest.model.helper.OrmLiteHelper;

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

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);
    }
}
