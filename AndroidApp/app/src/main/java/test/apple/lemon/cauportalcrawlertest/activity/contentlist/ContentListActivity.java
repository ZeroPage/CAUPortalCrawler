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

import butterknife.ButterKnife;
import butterknife.InjectView;
import test.apple.lemon.cauportalcrawlertest.R;
import test.apple.lemon.cauportalcrawlertest.model.EClassContent;
import test.apple.lemon.cauportalcrawlertest.model.TempSC;

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
                // todo image view with glide...
                return view;
            }
        };
        TempSC instance = TempSC.getInstance();
        adapter.addAll(instance);
        listView.setAdapter(adapter);
    }

    static class ViewHolder {
        @InjectView(R.id.isReadImageView)
        ImageView isReadImageView;
        @InjectView(R.id.titleTextView)
        TextView titleTextView;
        @InjectView(R.id.categoryTextView)
        TextView categoryTextView;

        public ViewHolder(View view) {
            ButterKnife.inject(this,view);
        }
    }

}
