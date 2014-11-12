package test.apple.lemon.cauportalcrawlertest.model.helper;

import android.widget.BaseAdapter;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;

/**
 * Created by rino0601 on 2014. 11. 12..
 */

public abstract class OrmLiteAdapter<T, ID> extends BaseAdapter {

    private RuntimeExceptionDao<T, ID> dao;
    private AndroidDatabaseResults dbResults;


    public OrmLiteAdapter(RuntimeExceptionDao<T, ID> dao, PreparedQuery<T> preparedQuery) {
        this.dao = dao;
        dbResults = (AndroidDatabaseResults) dao.iterator(preparedQuery).getRawResults();
    }


    @Override
    public int getCount() {
        return dbResults.getCount();
    }


    @Override
    public T getItem(final int position) {
        dbResults.moveAbsolute(position);
        return dao.mapSelectStarRow(dbResults);
    }


    @Override
    public long getItemId(final int position) {
        return position;
    }
}
