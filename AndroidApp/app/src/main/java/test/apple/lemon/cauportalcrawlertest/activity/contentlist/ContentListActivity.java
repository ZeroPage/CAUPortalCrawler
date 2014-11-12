package test.apple.lemon.cauportalcrawlertest.activity.contentlist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

public class ContentListActivity extends Activity {

    @InjectView(R.id.listView)
    ListView listView;

    public static void start(Context context) {
        Intent intent = new Intent(context, ContentListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);
        ButterKnife.inject(this);

        ArrayAdapter<EClassContent> adapter = new ArrayAdapter<EClassContent>(this, R.layout.item_content_list) {
            @Override
            public View getView(int position, View view, ViewGroup parent) {
                ViewHolder holder;
                if (view != null) {
                    holder = (ViewHolder) view.getTag();
                } else {
                    view = getLayoutInflater().inflate(R.layout.item_content_list, parent, false);
                    holder = new ViewHolder(view);
                    view.setTag(holder);
                }

                EClassContent item = getItem(position);
                holder.categoryTextView.setText(item.getLecture() + " > " + item.getBoard());
                holder.titleTextView.setText(item.getTitle());
                int res = item.isAlreadyRead() ? R.drawable.ic_action_read : R.drawable.ic_action_unread;
                Glide.with(ContentListActivity.this).load(res).into(holder.isReadImageView);
                return view;
            }
        };

        try {
            RuntimeExceptionDao<EClassContent, Integer> dao = AppDelegate.getHelper(getApplicationContext()).getContentsDAO();
            PreparedQuery<EClassContent> query = dao.queryBuilder().orderBy(EClassContent.DATETIME_FIELD, false).prepare();
            adapter.addAll(dao.query(query));
        } catch (SQLException ignored) {
            ignored.printStackTrace();
        }finally {
            listView.setAdapter(adapter);
        }
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
