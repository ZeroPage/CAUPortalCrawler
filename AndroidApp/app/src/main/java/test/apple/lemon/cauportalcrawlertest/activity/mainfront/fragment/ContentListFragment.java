package test.apple.lemon.cauportalcrawlertest.activity.mainfront.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;

import java.sql.SQLException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import test.apple.lemon.cauportalcrawlertest.AppDelegate;
import test.apple.lemon.cauportalcrawlertest.R;
import test.apple.lemon.cauportalcrawlertest.model.EClassContent;
import test.apple.lemon.cauportalcrawlertest.model.helper.OrmLiteAdapter;

/**
 * Created by rino0601 on 2014. 11. 12..
 */
public class ContentListFragment extends Fragment {

    @InjectView(R.id.listView)
    ListView listView;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_content_list, container, false);
        ButterKnife.inject(this, rootView);
        try {
            RuntimeExceptionDao<EClassContent, Integer> dao = AppDelegate.getHelper(getActivity()).getContentsDAO();
            PreparedQuery<EClassContent> query = dao.queryBuilder().orderBy(EClassContent.DATETIME_FIELD, false).prepare();
            Adapter adapter = new Adapter(getActivity(), dao, query);
            listView.setAdapter(adapter);
        } catch (SQLException ignored) {
            ignored.printStackTrace();
        }
        return rootView;
    }

    static class Adapter extends OrmLiteAdapter<EClassContent, Integer> { // 일단 지금은 reload를 지원하지 않음.

        private final Context context;

        public Adapter(Context context, RuntimeExceptionDao<EClassContent, Integer> contentsDAO, PreparedQuery<EClassContent> preparedQuery) {
            super(contentsDAO, preparedQuery);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;
            if (view != null) {
                holder = (ViewHolder) view.getTag();
            } else {
                view = LayoutInflater.from(context).inflate(R.layout.item_content_list, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }

            EClassContent item = getItem(position);
            holder.categoryTextView.setText(item.getLecture() + " > " + item.getBoard());
            holder.titleTextView.setText(item.getTitle());
            int res = item.isAlreadyRead() ? R.drawable.ic_action_read : R.drawable.ic_action_unread;
            Glide.with(context).load(res).into(holder.isReadImageView);
            return view;
        }

        static class ViewHolder {
            @InjectView(R.id.isReadImageView)
            ImageView isReadImageView;
            @InjectView(R.id.titleTextView)
            TextView titleTextView;
            @InjectView(R.id.categoryTextView)
            TextView categoryTextView;

            public ViewHolder(View view) {
                ButterKnife.inject(this, view);
            }
        }
    }
}
