package test.apple.lemon.cauportalcrawlertest.model.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import test.apple.lemon.cauportalcrawlertest.model.EClassContent;
import timber.log.Timber;

/**
 * Created by rino0601 on 2014. 11. 12..
 */
public class OrmLiteHelper extends OrmLiteSqliteOpenHelper {
    public static final String DATABASE_NAME = "orm_lite.db";
    private static final int DATABASE_VERSION = 1;

    private RuntimeExceptionDao<EClassContent, Integer> contentsDAO;

    public OrmLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            Timber.i(OrmLiteHelper.class.getName(), "onCreate");
            TableUtils.createTableIfNotExists(connectionSource, EClassContent.class);
        } catch (SQLException e) {
            Timber.e(OrmLiteHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Timber.i(OrmLiteHelper.class.getName(), "Enter to onUpgrade");
            int upgradeTo = oldVersion + 1;
            while (upgradeTo <= newVersion) {
                switch (upgradeTo) {
                    default: {
                        Timber.i("ORMLite Table 초기화. 다 갈아엎어!");
                        dropAllTables(database, connectionSource);
                    }
                    break;
                }
                upgradeTo++;
            }
            Timber.i(OrmLiteHelper.class.getName(), "Upgrading to version " + (upgradeTo - 1));
            Timber.i(OrmLiteHelper.class.getName(), "Exit from onCreate");
        } catch (Exception e) {
            Timber.e(e, "SQLite Table Update 실패.");
            dropAllTables(database, connectionSource);
        }
    }

    private void dropAllTables(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        sqLiteDatabase.rawQuery("select 'drop table if exists' || name || ';'" +
                "from sqlite_master where type = 'table';", null);
        onCreate(sqLiteDatabase, connectionSource);
    }

    public RuntimeExceptionDao<EClassContent, Integer> getContentsDAO() {
        if (contentsDAO == null) {
            contentsDAO = getRuntimeExceptionDao(EClassContent.class);
        }
        return contentsDAO;
    }
}
