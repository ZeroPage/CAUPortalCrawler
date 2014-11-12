package test.apple.lemon.cauportalcrawlertest.model.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import test.apple.lemon.cauportalcrawlertest.BuildConfig;
import test.apple.lemon.cauportalcrawlertest.model.LocalProperties;

/**
 * Created by rino0601 on 2014. 9. 17..
 */
public class PrefHelper {
    private static final String PREF_NAME = BuildConfig.PACKAGE_NAME + ".pref.helper";

    private static PrefHelper instance;

    private final Gson gson;
    private final SharedPreferences mPrefs;
    private final Context context;

    // ------------------------ DAO 등록 하는 곳 ------------------------//

    // ------------------------ DAO 등록 하는 곳 ------------------------//

    private PrefHelper(Context context) { // App에 등록해두고 쓸 것.
        this.context = context.getApplicationContext();
        gson = new Gson();
        mPrefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static PrefHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (PrefHelper.class) {
                if (instance == null)
                    instance = new PrefHelper(context);
            }
        }
        return instance;
    }

    // ------------------------ DAO 등록 하는 곳 ------------------------//
    public PrefDao<LocalProperties> getPrefDao() {
        return new PrefDao<LocalProperties>(LocalProperties.class) {
            @Override
            protected LocalProperties defaultData() {
                return new LocalProperties();
            }
        };
    }
    // ------------------------ DAO 등록 하는 곳 ------------------------//


    public class PrefDao<T> {
        private final Class<T> aClass;

        public PrefDao(Class<T> aClass) {
            this.aClass = aClass;
        }

        public T loadData() {
            String json = mPrefs.getString(getKeyName(), null);
            if (json == null) {
                return defaultData();
            }
            return gson.fromJson(json, aClass);
        }

        protected String getKeyName() {
            return aClass.getName();
        }

        protected T defaultData() {
            // fixme DefaultSharedPreferences 를 사용 할 것.
            return null;
        }

        public void saveData(T data) {
            String json = gson.toJson(data);
            SharedPreferences.Editor edit = mPrefs.edit();
            edit.putString(getKeyName(), json);
            edit.commit();
        }
    }
}
